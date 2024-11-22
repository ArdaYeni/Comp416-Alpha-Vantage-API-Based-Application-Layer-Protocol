import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.time.LocalDate;

public class Alpha416Server {
    private static final int PORT = 4160;
    private static final String API_KEY = "QIXLEZC0G9AD2QW3";

    public static void main(String[] args) {
        ExecutorService clientHandlingPool = Executors.newCachedThreadPool();//part responsible for the threads
        //server will be started in 4160
        try (ServerSocket serverSocket = new ServerSocket(4160)) {
            System.out.println("Alpha416 Server is  now ..");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(30000); // Set a 30-second timeout for each client socket
                clientHandlingPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("error happended due to server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            System.out.println("a new client is connected: " + clientSocket.getRemoteSocketAddress());
            String cMessage="";
            while ((cMessage = in.readLine()) != null) {
                System.out.println("Command recieved: " + cMessage);
                if (cMessage.startsWith("Alpha416")) {
                    String res = processRequest(cMessage);
                    out.println(res);
                } else {
                    out.println("Alpha416 ALPHA_400 Invalid Request\nError: Unsupported command.\nEND_OF_MESSAGE");
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Connection timed out for client: " + clientSocket.getRemoteSocketAddress());
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                // I couldnt handle this part
                out.println("Alpha416 ALPHA_408 Request Timeout\nError: Your session has timed out due to no activity.\nEND_OF_MESSAGE");
                out.flush();
                Thread.sleep(100);
            } catch (IOException | InterruptedException ioException) {
                System.err.println("Error " + ioException.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Disconnected from client: " + clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                System.err.println("Error.Socket is closing " + e.getMessage());
            }
        }
    }




    private String processRequest(String message) {
        System.out.println("Processing message: " + message);

        String[] tokens = message.split(" ");

        if (tokens.length < 2 || !tokens[0].equals("Alpha416")) {
            return "Alpha416 ALPHA_400 Invalid Request\nError: Invalid format.\nEND_OF_MESSAGE";
            //if there are not enough parameters, it will give ALPHA_400
        }

        String command = tokens[1];
        System.out.println("Detected command: " + command);

        if (command.equals("EXC")) {
            return handleExchangeRateRequest(tokens);
        } else if (command.equals("GAS")) {
            //natural gas
            return handleGasPriceRequest(tokens);
        } else if (command.equals("QUIT")) {
            return "Alpha416 ALPHA_200 Success!\nDisconnected from Alpha416.\nEND_OF_MESSAGE";
        } else {
            return "Alpha416 ALPHA_400 Invalid Request\nError: Invalid METHOD.\nEND_OF_MESSAGE";
        }
    }

    private String handleExchangeRateRequest(String[] tokens) {
        //these are control flags for further functions

        boolean includeFromNameFlag = false;
        boolean includeToNameFlag = false;
        String fromCurrencyFlag = null;
        String toCurrencyFlag = null;

        // parsing tokens for 'from', 'to', 'from_name', and 'to_name' parameters
        int i = 2;  // token[0] is Alpha416 token[1] is command
        while (i < tokens.length) {
            if (tokens[i].equals("-from")) {

                if (i + 1 < tokens.length) {
                    fromCurrencyFlag = tokens[i + 1];
                    i++;
                } else {
                    return "Alpha416 ALPHA_400 Invalid Request\nError: Missing 'from' currency.\nEND_OF_MESSAGE";
                }
            } else if (tokens[i].equals("-to")) {
                if (i + 1 < tokens.length) {
                    toCurrencyFlag = tokens[i + 1];
                    i++;
                } else {
                    return "Alpha416 ALPHA_400 Invalid Request\nError: Missing 'to' currency.\nEND_OF_MESSAGE";
                }
            } else if (tokens[i].equals("-from_name")) {
                includeFromNameFlag = true;
            } else if (tokens[i].equals("-to_name")) {
                includeToNameFlag = true;
            } else {
                System.out.println(" unrecognized tokens: " + tokens[i]);
                //this is for the uncrecognized tokens
            }
            i++;
        }


        if (fromCurrencyFlag == null || toCurrencyFlag == null) {
            return "Alpha416 ALPHA_400 Invalid Request\nError: Missing parameters.\nEND_OF_MESSAGE";
        }

        try {
            // if parsing is succesfull, code wll make a connection with api
            String apiUrl = "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency="
                    + fromCurrencyFlag + "&to_currency=" + toCurrencyFlag + "&apikey=QIXLEZC0G9AD2QW3";
            String apiResponse = fetchFromApi(apiUrl);
            String newResponse = parseExchangeRateResponse(apiResponse, includeFromNameFlag, includeToNameFlag);

            return "Alpha416 ALPHA_200 Success\n" + newResponse + "\nEND_OF_MESSAGE";
        } catch (Exception e) {
            System.err.println("Error processing exchange rate request: " + e.getMessage());
            e.printStackTrace();
            return "Alpha416 ALPHA_500 Server Fault\nError: Unable to process request.\nEND_OF_MESSAGE";
        }
    }
    private String calculatePriceChange(String apiResponse, String date1, String date2) {
        try {
            JSONObject json_api = new JSONObject(apiResponse);
            JSONArray monthlyPrices = json_api.getJSONArray("data");

            Double price1 = null;
            Double price2 = null;

            for (int i = 0; i < monthlyPrices.length(); i++) {
                JSONObject priceEntry = monthlyPrices.getJSONObject(i);
                String entryDate = priceEntry.getString("date");
                double value = priceEntry.getDouble("value");

                if (entryDate.equals(date1)) {
                    price1 = value;
                } else if (entryDate.equals(date2)) {
                    price2 = value;
                }


                if (price1 != null && price2 != null)
                    break;
            }

          /*  if (price1 == null || price2 == null) {
                return "Alpha416 ALPHA_404 Not Found\nError: Data not available for the specified dates.\nEND_OF_MESSAGE";
            }*/

            String comparisonResult;
            if (price1 < price2) {
                comparisonResult = "gas price increased from " + date1 + " to " + date2 + ".\nPrice on " + date1 + ": " + price1 + "\nPrice on " + date2 + ": " + price2;
            } else if (price1 > price2) {
                comparisonResult = "Natural gas price decreased from " + date1 + " to " + date2 + ".\nPrice on " + date1 + ": " + price1 + "\nPrice on " + date2 + ": " + price2;
            } else {
                comparisonResult = "Natural gas price remained the same on both dates.\nPrice on " + date1 + ": " + price1 + "\nPrice on " + date2 + ": " + price2;
            }

            return comparisonResult;
        } catch (JSONException e) {
            System.err.println("Error parsing JSON response for natural gas change calculation: " + e.getMessage());
            e.printStackTrace();
            return "Alpha416 ALPHA_500 Server Fault\nError: Unable to parse API response.";
        }
    }

    private String handleGasPriceRequest(String[] tokens) {
        String requestedDate = null;
        String comparedDate = null;
        String startingDate = null;
        String endingDate = null;
        boolean calculateAverageFlag = false;
        boolean calculateChangeFlag = false;

        int i = 2;
        //checking for -average -date -change parameteres
        while (i < tokens.length) {
            if (tokens[i].equals("-date")) {
                if (i + 1 < tokens.length) {
                    requestedDate = tokens[i + 1];
                    i++;
                } else {
                    return "Alpha416 ALPHA_400 Invalid Request\nError: Missing date parameter.\nEND_OF_MESSAGE";
                }
            } else if (tokens[i].equals("-average")) {
                if (i + 2 < tokens.length) {
                    startingDate  = tokens[i + 1];
                    endingDate = tokens[i + 2];
                    calculateAverageFlag = true;
                    i += 2;
                } else {
                    return "Alpha416 ALPHA_400 Invalid Request\nError: Missing dates for average calculation.\nEND_OF_MESSAGE";
                }
            } else if (tokens[i].equals("-change")) {
                if (i + 1 < tokens.length) {
                    comparedDate = tokens[i + 1];
                    calculateChangeFlag  = true;
                    i++;
                } else {
                    return "Alpha416 ALPHA_400 Invalid Request\nError: Missing date for change calculation.\nEND_OF_MESSAGE";
                }
            }
            i++;
        }

        try {
            String apiUrl = "https://www.alphavantage.co/query?function=NATURAL_GAS&interval=monthly&apikey=QIXLEZC0G9AD2QW3";
            String apiResponse = fetchFromApi(apiUrl);

            String parsedResponse;
            if (calculateAverageFlag && startingDate  != null && endingDate != null) {
                parsedResponse = calculateAverageNaturalGasPrice(apiResponse, startingDate , endingDate);
            } else if (calculateChangeFlag  && requestedDate != null && comparedDate != null) {
                parsedResponse = calculatePriceChange(apiResponse, requestedDate, comparedDate);
            } else if (requestedDate != null) {
                parsedResponse = parseNaturalGasResponse(apiResponse, requestedDate);
            } else {
                parsedResponse = parseNaturalGasResponse(apiResponse, null);
            }

            return "Alpha416 ALPHA_200 Success!\n" + parsedResponse + "\nEND_OF_MESSAGE";
        } catch (Exception e) {
            System.err.println("Error processing natural gas price request: " + e.getMessage());
            e.printStackTrace();
            return "Alpha416 ALPHA_500 Server Fault:(\nError: Unable to process request.\nEND_OF_MESSAGE";
        }
    }





    private String fetchFromApi(String apiUrl) throws IOException {
        System.out.println("Fetching data from API: " + apiUrl);

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while (true) {
                line = in.readLine();
                if (line == null) {
                    break;
                }
                response.append(line);
            }

            System.out.println("API Response: " + response.toString());
            return response.toString();
        } catch (IOException e) {
            System.err.println("Error fetching API data: " + e.getMessage());
            throw e;
        }
    }

    private String parseExchangeRateResponse(String apiResponse, boolean includeFromName, boolean includeToName) {
        try {
            JSONObject json = new JSONObject(apiResponse);
            //this exchange data consists of 6 different header
            JSONObject exchangeRateDat = json.getJSONObject("Realtime Currency Exchange Rate");

            String fromCurrencyCode = exchangeRateDat.getString("1. From_Currency Code");
            String toCurrencyCode = exchangeRateDat.getString("3. To_Currency Code");
            String exchangeRate = exchangeRateDat.getString("5. Exchange Rate");
            String lastRefreshed = exchangeRateDat.getString("6. Last Refreshed");

            StringBuilder response = new StringBuilder("Exchange Rate Info:\n");


            if (includeFromName) {
                String fromCurrencyName = exchangeRateDat.getString("2. From_Currency Name");
                response.append("From Currency: ").append(fromCurrencyCode).append(" (").append(fromCurrencyName).append(")\n");
            } else {
                response.append("From Currency: ").append(fromCurrencyCode).append("\n");
            }

            if (includeToName) {
                String toCurrencyName = exchangeRateDat.getString("4. To_Currency Name");
                response.append("To the Currency: ").append(toCurrencyCode).append(" (").append(toCurrencyName).append(")\n");
            } else {
                response.append("To the Currency: ").append(toCurrencyCode).append("\n");
            }

            response.append("Exchange Rate: ").append(exchangeRate).append("\n");
            response.append("Last Refreshed: ").append(lastRefreshed);

            return response.toString();
        } catch (JSONException e) {
            System.err.println("Error parsing JSON response for exchange rate: " + e.getMessage());
            e.printStackTrace();
            return "Alpha416 ALPHA_500 Server Fault\nError: Unable to parse API response.";
        }
    }

    private String parseNaturalGasResponse(String apiResponse, String requestedDate) {
        try {
            JSONObject json = new JSONObject(apiResponse);
            JSONArray monthlyPrices = json.getJSONArray("data");

            if (requestedDate != null) {
                for (int i = 0; i < monthlyPrices.length(); i++) {
                    JSONObject priceEntry = monthlyPrices.getJSONObject(i);
                    String date = priceEntry.getString("date");

                    if (date.equals(requestedDate)) {
                        double price = priceEntry.getDouble("value");
                        return "Natural Gas Price on the date of " + date + ":\nPrice: " + price;
                    }
                }
                return "Alpha416 ALPHA_404 Not Found\nError: No data available for the requested date.";
            } else {
                JSONObject latestPrice = monthlyPrices.getJSONObject(0);
                String date = latestPrice.getString("date");
                double price = latestPrice.getDouble("value");

                return "Latest Natural Gas Price:\nDate: " + date + "\nPrice: " + price;
            }
        } catch (JSONException e) {
            System.err.println("Error parsing JSON response for natural gas: " + e.getMessage());
            e.printStackTrace();
            return "Alpha416 ALPHA_500 Server Fault\nError: Unable to parse API response.";
        }
    }

    private String calculateAverageNaturalGasPrice(String apiResponse, String startDate, String endDate) {
        try {
            JSONObject json = new JSONObject(apiResponse);
            JSONArray monthlyPrices = json.getJSONArray("data");
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            double total = 0;
            int count = 0;

            int i = 0;
            while (i < monthlyPrices.length()) {
                JSONObject priceEntry = monthlyPrices.getJSONObject(i);
                LocalDate entryDate = LocalDate.parse(priceEntry.getString("date"));

                if ((entryDate.isEqual(start) || entryDate.isAfter(start)) && (entryDate.isEqual(end) || entryDate.isBefore(end))) {
                    total += priceEntry.getDouble("value");
                    count++;
                }

                i++;
            }


            if (count > 0) {
                double averagePrice = total / count;
                return "Average Gas Price is from " + startDate + " to " + endDate + ":\nAverage Price: " + averagePrice;
            } else {
                return "Alpha416 ALPHA_404 Not Found\nError: No data available for the specified date range.";
            }
        } catch (JSONException e) {
            System.err.println("Error parsing JSON response for natural gas average calculation: " + e.getMessage());
            e.printStackTrace();
            return "Alpha416 ALPHA_500 Server Fault\nError: Unable to parse API response.";
        }
    }
}
