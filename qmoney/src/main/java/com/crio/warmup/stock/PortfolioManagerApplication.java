
package com.crio.warmup.stock;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {


     private static RestTemplate restTemplate = new RestTemplate() ;

     @Autowired
     private static  PortfolioManager portfolioManager ;

    public static String getToken() {
      return "5edf17ecc7e44b4864f59c0e289661c3ff3856c5";
   }

   public static PortfolioTrade[] listToArray(List<PortfolioTrade> trades){
      return trades.stream().toArray(PortfolioTrade[]::new);
   }

  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>
  
   public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {

      PortfolioTrade[] trades = listToArray(readTradesFromJson(args[0]));
         LocalDate endDate = LocalDate.parse(args[1]);

         return Arrays.stream(trades).map(trade -> {         
            String url =  prepareUrl(trade ,endDate, getToken());
            TiingoCandle[] candles = restTemplate.getForObject(url, TiingoCandle[].class);           
            return new TotalReturnsDto(trade.getSymbol(), candles[candles.length-1].getClose());        
         }).sorted(Comparator.comparing(TotalReturnsDto::getClosingPrice))
         .map(TotalReturnsDto::getSymbol)
         .collect(Collectors.toList());
   }
   
  public static List<String> mainReadFile(String[] args) throws StreamReadException, DatabindException, IOException, URISyntaxException {
      List<String>  symbols  = new ArrayList<>();                                                                      
      PortfolioTrade[] trades = listToArray(readTradesFromJson(args[0]));
          for(PortfolioTrade trade: trades){          
           symbols.add(trade.getSymbol());
          }
      return symbols;    
   }
  

  public static File fileResolver(String string) throws StreamReadException, DatabindException, IOException{
     File file = new File("src/main/resources/"+string);
     return file;
  }
  
  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }



  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  // https://api.tiingo.com/tiingo/daily/aapl/prices?startDate=2019-01-02&token=Not logged-in or registered. Please login or register to see your API Token
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    
    File assFile =  fileResolver(filename);    
    ObjectMapper mapper = getObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    PortfolioTrade[] trades = mapper.readValue(assFile,PortfolioTrade[].class);         
    
      return Arrays.asList(trades);     
  }

  // TODO:
   // Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {    
    String url = "https://api.tiingo.com/tiingo/daily/"+ trade.getSymbol() +"/prices?startDate="+trade.getPurchaseDate()+"&endDate="+endDate+"&token="+token; 
    return url;
  }

//   private static void printJsonObject(Object object) throws IOException {
//     Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
//     ObjectMapper mapper = new ObjectMapper();
//     logger.info(mapper.writeValueAsString(object));
//   }


  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "trades.json";
    String toStringOfObjectMapper = "ObjectMapper";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "";


   return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
       toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
       lineNumberFromTestFileInStackTrace});
 }



  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.









  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> list) {
     return list.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size() -1).getClose();
  }



  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
     String url =  prepareUrl(trade ,endDate, token);
     TiingoCandle[] candles = restTemplate.getForObject(url, TiingoCandle[].class);

     List<Candle> candlesList = Arrays.asList(candles);
     return candlesList;
  }


  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException,RuntimeException {
         PortfolioTrade[] trades = listToArray(readTradesFromJson(args[0]));
         LocalDate endDate = LocalDate.parse(args[1]);

         return Arrays.stream(trades).map(trade -> {
            String url =  prepareUrl(trade ,endDate, getToken());            
            TiingoCandle[] candles =  restTemplate.getForObject(url, TiingoCandle[].class);                  
            
            double openPrice = candles[0].getOpen();
            double closePrice = candles[candles.length -1].getClose();

            AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, trade, openPrice, closePrice);
            return annualizedReturn;

         }).sorted(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed())
         .collect(Collectors.toList());
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
         double totalVal = (sellPrice - buyPrice) / buyPrice;
         double total_num_years = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.24;

         double annualized_returns = Math.pow((1 + totalVal), (1 / total_num_years)) - 1;
         
      return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalVal);
  }














  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
  }






  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
   
      //  Object portfolioTrades;
      List<PortfolioTrade> portfolioTrades = readTradesFromJson(file);
      return portfolioManager.calculateAnnualizedReturn(portfolioTrades, endDate);
  }

  }


