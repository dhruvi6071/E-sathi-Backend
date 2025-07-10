package com.example.ESathi.Serivces.wedherSevices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;



@Service
public class WeatherService {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getWeatherDataByPincode(String pincode) {
        String url = "https://api.openweathermap.org/data/2.5/weather?zip=" + pincode + ",IN&units=metric&appid=" + apiKey;
        return restTemplate.getForObject(url, String.class);
    }

    public JSONObject getWeatherJson(String pincode) {
        String response = getWeatherDataByPincode(pincode);
        return new JSONObject(response);
    }
}
