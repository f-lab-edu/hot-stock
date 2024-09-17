package com.bjcareer.search.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bjcareer.search.domain.entity.Stock;
import com.bjcareer.search.domain.entity.Thema;
import com.bjcareer.search.domain.entity.ThemaInfo;
import com.bjcareer.search.out.crawling.naver.CrawlingThema;
import com.bjcareer.search.repository.stock.StockRepository;
import com.bjcareer.search.repository.stock.ThemaInfoRepository;
import com.bjcareer.search.repository.stock.ThemaRepository;
import com.bjcareer.search.service.exceptions.InvalidStockInformation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockService {
	private final ThemaRepository themaRepository;
	private final ThemaInfoRepository themaInfoRepository;
	private final StockRepository stockRepository;
	private final CrawlingThema crawlingThema;

	public List<Thema> getStockOfThema(String stock) {
		log.debug("stock: {}", stock);
		return themaRepository.findByStockNameContaining(stock);
	}

	public Thema addStockThema(String stockCode, String stockName, String theme) {
		Optional<Stock> byStockCode = stockRepository.findByCode(stockCode);
		Stock stock = byStockCode.orElseGet(() -> {
			log.info("Stock not found, crawling stock information for stock: {} with code: {}", stockName, stockCode);
			Stock result = crawlingThema.getStock(stockCode, stockName);

			if (stockCode.equals(result.getCode()) && stockName.equals(result.getName()) && result.validStock()) {
				return stockRepository.save(result);
			}

			throw new InvalidStockInformation("Invalid stock information provided for stock: " + stockName + " with code: " + stockCode);
		});

		Optional<ThemaInfo> byThemaName = themaInfoRepository.findByThemaName(theme);
		ThemaInfo themaInfo = byThemaName.orElseGet(
			() -> themaInfoRepository.save(new ThemaInfo(theme, "USER CREATED")));

		return themaRepository.findByStockNameAndThemaName(stock.getName(), themaInfo.getName()).orElseGet(() -> themaRepository.save(new Thema(stock, themaInfo)));
	}
}
