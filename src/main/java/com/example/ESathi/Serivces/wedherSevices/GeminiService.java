package com.example.ESathi.Serivces.wedherSevices;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public JSONObject analyzeWeatherWithGemini(JSONObject weatherData, String areaName) {
        String prompt = """
                    You are a weather risk predictor for rural electric outages.
                    
                    I will provide you weather data in JSON. You must respond ONLY in raw JSON format with the following structure:
                    
                    {
                      "area": "<Area Name>",
                      "weather": "<Short weather description like: Overcast clouds>",
                      "risk": "<Risk level like Low/Medium/High with brief reason>"
                    }
                    
                    DO NOT return anything else. Do NOT add summary, explanation or extra lines. Just return valid JSON.
                    
                    Here is the weather data:
                    """ + weatherData.toString();

        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + geminiApiKey;

        JSONObject request = new JSONObject()
                .put("contents", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("parts", new org.json.JSONArray()
                                        .put(new JSONObject().put("text", prompt))
                                )
                        )
                );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);


        // Step 1: Parse the outer Gemini response
        JSONObject wrapped = new JSONObject(response.getBody());
        String jsonText = wrapped
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        if (jsonText.startsWith("```")) {
            jsonText = jsonText.replaceAll("(?s)```(?:json)?\\s*", "").replaceAll("```", "").trim();
        }

        if (!jsonText.startsWith("{")) {
            throw new RuntimeException("Gemini did not return JSON. Output was:\n" + jsonText);
        }

        // Step 2: Return the actual weather prediction JSON
        return new JSONObject(jsonText); // Now ready to use directly!
    }
}
