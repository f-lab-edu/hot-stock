package com.bjcareer.search.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bjcareer.search.controller.dto.SearchThemaResponseDTO;
import com.bjcareer.search.domain.entity.ThemaInfo;
import com.bjcareer.search.service.ThemaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v0/thema")
public class ThemaController {
	private final ThemaService themaService;

	@GetMapping("")
	public Response<?> searchThema(@RequestParam(name = "q") String themaKeyword,
		@RequestParam(required = false) Long marketAmount) {
		if (themaKeyword.isEmpty()) {
			return new Response<>(HttpStatus.BAD_REQUEST, "thema is empty", null);
		}
		List<ThemaInfo> themaInfos = themaService.searchThema(themaKeyword);
		SearchThemaResponseDTO searchThemaResponseDTO = new SearchThemaResponseDTO(themaInfos);
		return new Response<>(HttpStatus.OK, "OK", searchThemaResponseDTO.response);
	}
}
