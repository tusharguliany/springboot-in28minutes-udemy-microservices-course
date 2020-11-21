package com.guliany.microservices.controller;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.guliany.microservices.CurrencyExchangeServiceProxy;
import com.guliany.microservices.entity.CurrencyConversionBean;

@RestController
public class CurrencyConversionController {

	@Autowired
	private WebClient.Builder builder;
	
	@Autowired
	private CurrencyExchangeServiceProxy proxy;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@GetMapping("/currency-converter/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean convertCurrency(@PathVariable String from, @PathVariable("to") String to,
			@PathVariable(name = "quantity") BigDecimal quantity) {

		/*
		 * Preferred Approach
		 */
		CurrencyConversionBean currencyConversionBean = this.builder.build().get()
				.uri("http://localhost:8082/currency-exchange/from/" + from + "/to/" + to).retrieve()
				.bodyToMono(CurrencyConversionBean.class).block();

		/*
		 * Alternate Approach
		 */
//		Map<String, String> uriVariables = new HashMap<>();
//		uriVariables.put("from", from);
//		uriVariables.put("to", to);
//		ResponseEntity<CurrencyConversionBean> responseEntity = new RestTemplate().getForEntity("http://localhost:8082/currency-exchange/from/{from}/to/{to}",
//				CurrencyConversionBean.class, uriVariables);
//		CurrencyConversionBean currencyConversionBean = responseEntity.getBody();

		return new CurrencyConversionBean(1l, from, to, currencyConversionBean.getConversionMultiple(), quantity,
				quantity.multiply(currencyConversionBean.getConversionMultiple()), currencyConversionBean.getPort());
	}
	
	@GetMapping("/currency-converter-feign/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean convertCurrencyFeign(@PathVariable String from, @PathVariable("to") String to,
			@PathVariable(name = "quantity") BigDecimal quantity) {

		/*
		 * using feign client
		 */
		CurrencyConversionBean currencyConversionBean = this.proxy.retrieveExchangeValue(from, to);
		logger.info("{}", currencyConversionBean);
		return new CurrencyConversionBean(1l, from, to, currencyConversionBean.getConversionMultiple(), quantity,
				quantity.multiply(currencyConversionBean.getConversionMultiple()), currencyConversionBean.getPort());
	}

}
