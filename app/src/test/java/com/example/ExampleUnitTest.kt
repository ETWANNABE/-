package com.example

import com.example.data.domain.SimilarityAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testKoreanStemmingAndSimilarity() {
    val stem1 = SimilarityAnalyzer.normalizeStem("행복해요")
    val stem2 = SimilarityAnalyzer.normalizeStem("행복함")
    assertEquals("행복", stem1)
    assertEquals("행복", stem2)

    val sim = SimilarityAnalyzer.calculateSimilarity("행복해요", "행복함")
    assertTrue(sim >= 0.90f)
  }
}
