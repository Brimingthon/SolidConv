package com.solid.check;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class SolidCheckController {

	private final SolidCheckService solidCheckService;

	public SolidCheckController(SolidCheckService solidCheckService) {
		this.solidCheckService = solidCheckService;
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@PostMapping("/analyze")
	public String analyzeCode(@RequestParam("code") String code, Model model) {
		List<AnalyzedLine> analyzedCodeLines = solidCheckService.analyze(code);
		model.addAttribute("analyzedCodeLines", analyzedCodeLines);
		return "result";
	}


	@PostMapping("/upload")
	public String uploadFile(@RequestParam("file") MultipartFile file, Model model) throws IOException {
		String content = new String(file.getBytes());
		String result = solidCheckService.analyze(content).toString();
		model.addAttribute("originalCode", content);
		model.addAttribute("analyzedCode", result);
		return "result";
	}
}
