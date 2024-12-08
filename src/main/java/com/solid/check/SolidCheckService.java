package com.solid.check;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SolidCheckService {

	public List<AnalyzedLine> analyze(String code) {
		List<AnalyzedLine> analyzedLines = new ArrayList<>();
		try {
			JavaParser javaParser = new JavaParser();
			CompilationUnit compilationUnit = javaParser.parse(code).getResult()
					.orElseThrow(() -> new IllegalArgumentException("Unable to parse the provided code"));

			for (String line : code.split("\n")) {
				analyzedLines.add(new AnalyzedLine(line, false, ""));
			}

			List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
			for (ClassOrInterfaceDeclaration cls : classes) {
				analyzeClass(cls, analyzedLines);
			}
		} catch (Exception e) {
			analyzedLines.add(new AnalyzedLine("Error parsing code: " + e.getMessage(), true, "Parsing Error"));
		}

		return analyzedLines;
	}

	private void analyzeClass(ClassOrInterfaceDeclaration cls, List<AnalyzedLine> analyzedLines) {
		int classLine = cls.getBegin().map(pos -> pos.line).orElse(-1);
		StringBuilder classComments = new StringBuilder();

		// Check all SOLID principles for the class
		classComments.append(checkSingleResponsibility(cls));
		classComments.append(checkDependencyInversion(cls));
		classComments.append(checkOpenClosed(cls));
		classComments.append(checkLiskovSubstitution(cls));
		classComments.append(checkInterfaceSegregation(cls));

		if (classComments.length() > 0 && classLine > 0) {
			AnalyzedLine line = analyzedLines.get(classLine - 1);
			line.setError(true);
			line.setComment(classComments.toString());
		}

		// Analyze each method in the class
		for (MethodDeclaration method : cls.getMethods()) {
			analyzeMethod(method, analyzedLines);
		}
	}

	private void analyzeMethod(MethodDeclaration method, List<AnalyzedLine> analyzedLines) {
		int methodLine = method.getBegin().map(pos -> pos.line).orElse(-1);
		if (methodLine > 0) {
			StringBuilder methodComments = new StringBuilder();

			// SRP: Check for combining unrelated responsibilities
			if (method.getNameAsString().toLowerCase().contains("log") &&
					method.getNameAsString().toLowerCase().contains("db")) {
				methodComments.append("// SRP: Combines logging and database operations.\n");
			}

			// DIP: Check for direct instantiation of concrete classes
			if (method.getBody().isPresent() && method.getBody().get().toString().contains("new ")) {
				methodComments.append("// DIP: Instantiates concrete classes directly.\n");
			}

			// Add comments to the analyzed line
			if (methodComments.length() > 0) {
				AnalyzedLine line = analyzedLines.get(methodLine - 1);
				line.setError(true);
				line.setComment(methodComments.toString());
			}
		}
	}

	private String checkSingleResponsibility(ClassOrInterfaceDeclaration cls) {
		if (cls.getMethods().stream().anyMatch(method -> method.getNameAsString().equalsIgnoreCase("logEvent")) &&
				cls.getMethods().stream().anyMatch(method -> method.getNameAsString().equalsIgnoreCase("connectDB"))) {
			return "// SRP: Combines database, logging, and user operations.\n";
		}
		return "";
	}

	private String checkDependencyInversion(ClassOrInterfaceDeclaration cls) {
		boolean usesConcrete = cls.getMethods().stream()
				.flatMap(method -> method.getBody().stream()
						.flatMap(body -> body.findAll(ClassOrInterfaceDeclaration.class).stream()))
				.anyMatch(innerCls -> !innerCls.isInterface());
		if (usesConcrete) {
			return "// DIP: Depends on concrete classes.\n";
		}
		return "";
	}

	private String checkOpenClosed(ClassOrInterfaceDeclaration cls) {
		if (!cls.isAbstract() && cls.getMethods().stream().noneMatch(MethodDeclaration::isPublic)) {
			return "// OCP: No public methods for extensibility.\n";
		}
		return "";
	}

	private String checkLiskovSubstitution(ClassOrInterfaceDeclaration cls) {
		if (!cls.isInterface() && cls.getExtendedTypes().isEmpty() && cls.getImplementedTypes().isEmpty()) {
			return "// LSP: Not substitutable.\n";
		}
		return "";
	}

	private String checkInterfaceSegregation(ClassOrInterfaceDeclaration cls) {
		if (cls.isInterface() && cls.getMethods().size() > 10) {
			return "// ISP: Too many methods in interface.\n";
		}
		return "";
	}
}
