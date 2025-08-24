package com.example.ESathi.Serivces.wedherSevices;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private  String geminiApiKey;

    @Value("${gemini.url}")
    private  String GEMINI_API_URL;

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

    //        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + geminiApiKey;

         String url = GEMINI_API_URL + geminiApiKey;

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

    // for usage page
    public String getConsumptionAdvice(double averageUnit, double totalBills, double lastMonthUnit, String lastMonthStatus) {
        // Construct prompt for Gemini
        String prompt = """
        Analyze the user's electricity usage with the following data:

        Average monthly unit consumption over the past year: %s units
        Last month's unit consumption: %s units
        Total number of bills available: %s units
        Last bill payment status: %s

        Please calculate the percentage increase or decrease in last month's usage compared to the average, and include that in the response.

        Then generate a short and helpful electricity consumption message. Keep it:
        - Under 100 words
        - Clear, actionable, and user-friendly
        - Mention if last month’s usage increased or decreased compared to average
        - Include advice to improve consumption habits
        - Mention the unpaid status politely and responsibly
        """.formatted(
                averageUnit,
                lastMonthUnit,
                totalBills,
                lastMonthStatus
        );

        // Construct Gemini API request
        Map<String, Object> message = Map.of("parts", List.of(Map.of("text", prompt)));
        Map<String, Object> requestBody = Map.of("contents", List.of(message));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Send request to Gemini
        ResponseEntity<Map> response = restTemplate.exchange(
                GEMINI_API_URL + geminiApiKey,
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Parse and return Gemini's reply
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                Map<String, Object> firstCandidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");

                return parts.get(0).get("text");
            } catch (Exception e) {
                return "Failed to parse Gemini's response.";
            }
        } else {
            return "Gemini API error: " + response.getStatusCode();
        }
    }
}
