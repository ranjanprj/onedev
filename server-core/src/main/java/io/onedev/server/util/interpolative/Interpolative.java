package io.onedev.server.util.interpolative;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.server.util.interpolative.InterpolativeParser.SegmentContext;
import io.onedev.server.util.interpolative.Segment.Type;

public class Interpolative implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<Segment> segments;
	
	public Interpolative(List<Segment> segments) {
		this.segments = segments;
	}
	
	public static Interpolative parse(String interpolativeString) {
		CharStream is = CharStreams.fromString(interpolativeString); 
		InterpolativeLexer lexer = new InterpolativeLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new RuntimeException("Malformed interpolative: " + interpolativeString);
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		InterpolativeParser parser = new InterpolativeParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		
		List<Segment> segments = new ArrayList<>();
		for (SegmentContext segment: parser.interpolative().segment()) {
			if (segment.Literal() != null) 
				segments.add(new Segment(Type.LITERAL, segment.Literal().getText()));
			else
				segments.add(new Segment(Type.VARIABLE, FenceAware.unfence(segment.Variable().getText())));
		}
		return new Interpolative(segments);
	}

	public List<Segment> getSegments(@Nullable Type type) {
		return segments.stream().filter(it -> type==null || it.getType() == type).collect(Collectors.toList());
	}
	
	public String interpolateWith(Function<String, String> interpolator) {
		StringBuilder builder = new StringBuilder();
		for (Segment segment: segments) {
			if (segment.getType() == Type.LITERAL) {
				builder.append(segment.getContent());
			} else {
				String interpolated = interpolator.apply(segment.getContent()); 
				if (interpolated != null)
					builder.append(interpolated);
			}
		}
		return builder.toString();
	}
	
}