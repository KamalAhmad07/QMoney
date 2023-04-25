
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.PortfolioManagerApplication;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {
  
    private RestTemplate restTemplate;

   public TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest
  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws StockQuoteServiceException {      
      Candle[] result = null;
        try{
        String url = buildUri(symbol,from, to);
        String response = restTemplate.getForObject(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        result = mapper.readValue(response,TiingoCandle[].class);
        if(result == null)
            return new ArrayList<>();
         System.out.println("list = "+Arrays.asList(result)); 
        }catch(NullPointerException e){
          throw new StockQuoteServiceException("Error occured when requesting response from Tiingo Api", e.getCause());
        }
        catch(JsonProcessingException e){
         // throw new StockQuoteServiceException("Json Excpetion Occur  from Tiingo Api", e.getCause());
         throw new StockQuoteServiceException(e.getMessage());
        }
       return Arrays.asList(result);

  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String url = "https://api.tiingo.com/tiingo/daily/"+ symbol +"/prices?startDate="+startDate+"&endDate="+endDate+"&token="+PortfolioManagerApplication.getToken(); 
    //System.err.println("url = "+ url);
    return url;    
}


}
