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
		try {
			// Перевіряємо, чи файл не порожній
			if (file.isEmpty()) {
				System.out.println("Файл порожній.");
				model.addAttribute("error", "Uploaded file is empty!");
				return "result";
			}

			// Зчитуємо вміст файлу
			String content = new String(file.getBytes());
			System.out.println("Завантажений код: \n" + content);

			// Аналізуємо код
			List<AnalyzedLine> analyzedCodeLines = solidCheckService.analyze(content);

			// Додаємо дані в модель
			model.addAttribute("originalCode", content);
			model.addAttribute("analyzedCodeLines", analyzedCodeLines);

			System.out.println("Аналіз завершено успішно.");
			return "result";
		} catch (Exception e) {
			// Логування помилки
			System.err.println("Помилка при обробці файлу: " + e.getMessage());
			model.addAttribute("error", "Error processing the file: " + e.getMessage());
			return "result";
		}
	}


}
