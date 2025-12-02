package com.ramankumar.moviefinder.api

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.ramankumar.moviefinder.model.GeminiKeywordResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject

class GeminiService(private val apiKey: String) {

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
        }
    )

    private val gson = Gson()

    suspend fun convertNaturalLanguageToKeywords(query: String): Result<GeminiKeywordResponse> {
        return try {
            val prompt = buildPrompt(query)

            val response = model.generateContent(prompt)
            val responseText = response.text ?: ""

            // Parse JSON from response
            val parsedResponse = parseGeminiResponse(responseText)

            Result.success(parsedResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(query: String): String {
        return """
You are a movie search assistant. Convert the user's natural language query into structured search parameters for a movie database.

User query: "$query"

Return ONLY a valid JSON object (no markdown, no code blocks) with these fields:
- keywords: Array of specific movie-related keywords or themes (e.g., ["friendship", "coming-of-age", "high-school"])
- genres: Array of movie genres (use only these: action, adventure, animation, comedy, crime, documentary, drama, family, fantasy, history, horror, music, mystery, romance, science fiction, thriller, war, western)
- vibes: Array of mood/atmosphere descriptors (e.g., ["lighthearted", "dark", "inspiring"])
- referenceMovie: String containing the EXACT title of a specific movie being referenced (if any), or null if the query is general. Examples: "Saving Private Ryan", "The Shawshank Redemption", "Inception"

Rules:
1. Return ONLY the JSON object, nothing else
2. Use lowercase for all values EXCEPT referenceMovie (use proper title case)
3. Be specific with keywords - think about plot elements, themes, settings, character types
4. Limit to 3-5 keywords, 1-3 genres, 1-3 vibes
5. If the query mentions a specific movie title, extract it into referenceMovie field
6. Keywords should be relevant to TMDB's keyword database (themes, plot elements, settings, character archetypes)
7. ONLY return a reference movie if there is reasonable likelihood of a relevant movie
Examples:
User query: "feel-good movies about friendship"
{"keywords":["friendship","feel-good","heartwarming"],"genres":["comedy","family"],"vibes":["lighthearted","uplifting"],"referenceMovie":null}

User query: "war movies like Saving Private Ryan"
{"keywords":["world war ii","military","heroism"],"genres":["war","drama","action"],"vibes":["intense","realistic"],"referenceMovie":"Saving Private Ryan"}

User query: "movies similar to Inception"
{"keywords":["dream","heist","mind-bending"],"genres":["science fiction","thriller","action"],"vibes":["complex","cerebral"],"referenceMovie":"Inception"}

Now process the user's query and return ONLY the JSON object:
        """.trimIndent()
    }

    private fun parseGeminiResponse(responseText: String): GeminiKeywordResponse {
        try {
            //Remove markdown code blocks if present
            val cleanedJson = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()


            return gson.fromJson(cleanedJson, GeminiKeywordResponse::class.java)
        } catch (e: JsonSyntaxException) {

            //Fallback: Try to extract JSON manually
            return try {
                val jsonObject = JSONObject(responseText.substringAfter("{").substringBeforeLast("}").let { "{$it}" })

                val keywords = mutableListOf<String>()
                val genres = mutableListOf<String>()
                val vibes = mutableListOf<String>()

                if (jsonObject.has("keywords")) {
                    val keywordArray = jsonObject.getJSONArray("keywords")
                    for (i in 0 until keywordArray.length()) {
                        keywords.add(keywordArray.getString(i))
                    }
                }

                if (jsonObject.has("genres")) {
                    val genreArray = jsonObject.getJSONArray("genres")
                    for (i in 0 until genreArray.length()) {
                        genres.add(genreArray.getString(i))
                    }
                }

                if (jsonObject.has("vibes")) {
                    val vibeArray = jsonObject.getJSONArray("vibes")
                    for (i in 0 until vibeArray.length()) {
                        vibes.add(vibeArray.getString(i))
                    }
                }

                val referenceMovie = if (jsonObject.has("referenceMovie") && !jsonObject.isNull("referenceMovie")) {
                    jsonObject.getString("referenceMovie")
                } else {
                    null
                }

                GeminiKeywordResponse(keywords, genres, vibes, referenceMovie)
            } catch (fallbackError: Exception) {
                throw JsonSyntaxException("Failed to parse Gemini response: $responseText", e)
            }
        }
    }
}
