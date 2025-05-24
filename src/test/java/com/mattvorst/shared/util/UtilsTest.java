package com.mattvorst.shared.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UtilsTest {

    @Test
    void empty_ReturnsTrue_WhenStringIsNull() {
        assertTrue(Utils.empty((String) null));
    }

    @Test
    void empty_ReturnsTrue_WhenStringIsEmpty() {
        assertTrue(Utils.empty(""));
    }

    @Test
    void empty_ReturnsTrue_WhenStringIsWhitespace() {
        assertTrue(Utils.empty("   "));
    }

    @Test
    void empty_ReturnsFalse_WhenStringHasContent() {
        assertFalse(Utils.empty("content"));
    }

    @Test
    void empty_ReturnsFalse_WhenStringHasContentWithSpaces() {
        assertFalse(Utils.empty(" content "));
    }

    @Test
    void empty_Collection_ReturnsTrue_WhenCollectionIsNull() {
        assertTrue(Utils.empty((Collection<?>) null));
    }

    @Test
    void empty_Collection_ReturnsTrue_WhenCollectionIsEmpty() {
        assertTrue(Utils.empty(new ArrayList<>()));
    }

    @Test
    void empty_Collection_ReturnsFalse_WhenCollectionHasElements() {
        List<String> list = Arrays.asList("item1", "item2");
        assertFalse(Utils.empty(list));
    }

    @Test
    void empty_Map_ReturnsTrue_WhenMapIsNull() {
        assertTrue(Utils.empty((Map<?, ?>) null));
    }

    @Test
    void empty_Map_ReturnsTrue_WhenMapIsEmpty() {
        assertTrue(Utils.empty(new HashMap<>()));
    }

    @Test
    void empty_Map_ReturnsFalse_WhenMapHasEntries() {
        Map<String, String> map = Map.of("key", "value");
        assertFalse(Utils.empty(map));
    }

    @Test
    void equal_String_ReturnsTrue_WhenBothNull() {
        assertTrue(Utils.equal((String) null, (String) null));
    }

    @Test
    void equal_String_ReturnsFalse_WhenOneIsNull() {
        assertFalse(Utils.equal("test", null));
        assertFalse(Utils.equal(null, "test"));
    }

    @Test
    void equal_String_ReturnsTrue_WhenBothEqual() {
        assertTrue(Utils.equal("test", "test"));
    }

    @Test
    void equal_String_ReturnsFalse_WhenDifferent() {
        assertFalse(Utils.equal("test1", "test2"));
    }

    @Test
    void equal_UUID_ReturnsTrue_WhenBothNull() {
        assertTrue(Utils.equal((UUID) null, (UUID) null));
    }

    @Test
    void equal_UUID_ReturnsFalse_WhenOneIsNull() {
        UUID uuid = UUID.randomUUID();
        assertFalse(Utils.equal(uuid, null));
        assertFalse(Utils.equal(null, uuid));
    }

    @Test
    void equal_UUID_ReturnsTrue_WhenBothEqual() {
        UUID uuid = UUID.randomUUID();
        assertTrue(Utils.equal(uuid, uuid));
    }

    @Test
    void equal_UUID_ReturnsFalse_WhenDifferent() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        assertFalse(Utils.equal(uuid1, uuid2));
    }

    @Test
    void uuidFromString_ReturnsUUID_WhenValidString() {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        UUID result = Utils.uuidFromString(uuidString);
        assertNotNull(result);
        assertEquals(uuidString, result.toString());
    }

    @Test
    void uuidFromString_ReturnsNull_WhenInvalidString() {
        UUID result = Utils.uuidFromString("invalid-uuid");
        assertNull(result);
    }

    @Test
    void uuidFromString_ReturnsNull_WhenEmptyString() {
        UUID result = Utils.uuidFromString("");
        assertNull(result);
    }

    @Test
    void uuidFromString_ReturnsNull_WhenNullString() {
        UUID result = Utils.uuidFromString(null);
        assertNull(result);
    }

    @Test
    void stringFromUuid_ReturnsString_WhenValidUUID() {
        UUID uuid = UUID.randomUUID();
        String result = Utils.stringFromUuid(uuid);
        assertEquals(uuid.toString(), result);
    }

    @Test
    void stringFromUuid_ReturnsNull_WhenNullUUID() {
        String result = Utils.stringFromUuid(null);
        assertNull(result);
    }

    @Test
    void safeToBoolean_ReturnsTrue_WhenStringIsTrue() {
        assertTrue(Utils.safeToBoolean("TRUE"));
        assertTrue(Utils.safeToBoolean("true"));
        assertTrue(Utils.safeToBoolean("True"));
    }

    @Test
    void safeToBoolean_ReturnsFalse_WhenStringIsFalse() {
        assertFalse(Utils.safeToBoolean("FALSE"));
        assertFalse(Utils.safeToBoolean("false"));
        assertFalse(Utils.safeToBoolean("False"));
    }

    @Test
    void safeToBoolean_ReturnsFalse_WhenStringIsOther() {
        assertFalse(Utils.safeToBoolean("other"));
        assertFalse(Utils.safeToBoolean("yes"));
        assertFalse(Utils.safeToBoolean("1"));
    }

    @Test
    void safeToBoolean_ReturnsNull_WhenStringIsEmpty() {
        assertNull(Utils.safeToBoolean(""));
        assertNull(Utils.safeToBoolean("   "));
        assertNull(Utils.safeToBoolean(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "0", "-456"})
    void safeToInt_ReturnsCorrectValue_WhenValidString(String input) {
        int expected = Integer.parseInt(input);
        assertEquals(expected, Utils.safeToInt(input));
    }

    @Test
    void safeToInt_ReturnsZero_WhenInvalidString() {
        assertEquals(0, Utils.safeToInt("abc"));
        assertEquals(0, Utils.safeToInt("12.34"));
        assertEquals(0, Utils.safeToInt(""));
    }

    @Test
    void safeToInt_ReturnsZero_WhenNullString() {
        assertEquals(0, Utils.safeToInt((String) null));
    }

    @Test
    void safeToInt_Number_ReturnsCorrectValue() {
        assertEquals(123, Utils.safeToInt(123L));
        assertEquals(456, Utils.safeToInt(456.78));
    }

    @Test
    void safeToInt_Number_ReturnsZero_WhenNull() {
        assertEquals(0, Utils.safeToInt((Number) null));
    }

    @Test
    void asList_ReturnsNewList_WhenCollectionIsNotNull() {
        Collection<String> collection = Arrays.asList("a", "b", "c");
        List<String> result = Utils.asList(collection);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
        
        // Verify it's a new list, not the same reference
        assertNotSame(collection, result);
    }

    @Test
    void asList_ReturnsEmptyList_WhenCollectionIsNull() {
        List<String> result = Utils.asList((Collection<String>) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void asList_Array_ReturnsNewList_WhenArrayIsNotNull() {
        String[] array = {"a", "b", "c"};
        List<String> result = Utils.asList(array);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    @Test
    void asList_Array_ReturnsEmptyList_WhenArrayIsNull() {
        List<String> result = Utils.asList((String[]) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void asSet_ReturnsNewSet_WhenCollectionIsNotNull() {
        Collection<String> collection = Arrays.asList("a", "b", "c", "a");
        Set<String> result = Utils.asSet(collection);
        
        assertNotNull(result);
        assertEquals(3, result.size()); // Duplicates removed
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));
    }

    @Test
    void asSet_ReturnsEmptySet_WhenCollectionIsNull() {
        Set<String> result = Utils.asSet(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toCSV_Set_ReturnsConcatenatedString() {
        Set<String> set = Set.of("apple", "banana", "cherry");
        String result = Utils.toCSV(set);
        
        assertNotNull(result);
        assertTrue(result.contains("apple"));
        assertTrue(result.contains("banana"));
        assertTrue(result.contains("cherry"));
        assertTrue(result.contains(","));
    }

    @Test
    void toCSV_Set_ReturnsEmptyString_WhenSetIsEmpty() {
        Set<String> set = new HashSet<>();
        String result = Utils.toCSV(set);
        assertEquals("", result);
    }

    @Test
    void toCSV_Array_ReturnsConcatenatedString() {
        String[] array = {"apple", "banana", "cherry"};
        String result = Utils.toCSV(array);
        assertEquals("apple,banana,cherry", result);
    }

    @Test
    void toCSV_Array_SkipsNullValues() {
        String[] array = {"apple", null, "cherry"};
        String result = Utils.toCSV(array);
        assertEquals("apple,cherry", result);
    }

    @Test
    void uppercaseTrimmed_ReturnsUppercaseTrimmed_WhenValidString() {
        assertEquals("HELLO WORLD", Utils.uppercaseTrimmed("  hello world  "));
        assertEquals("TEST", Utils.uppercaseTrimmed("test"));
    }

    @Test
    void uppercaseTrimmed_ReturnsNull_WhenStringIsNull() {
        assertNull(Utils.uppercaseTrimmed(null));
    }

    @Test
    void uppercaseTrimmed_ReturnsNull_WhenStringIsEmpty() {
        assertNull(Utils.uppercaseTrimmed(""));
    }

    @Test
    void safeTrim_ReturnsTrimmed_WhenStringIsNotEmpty() {
        assertEquals("hello", Utils.safeTrim("  hello  "));
        assertEquals("test", Utils.safeTrim("test"));
    }

    @Test
    void safeTrim_ReturnsOriginal_WhenStringIsEmptyOrNull() {
        assertNull(Utils.safeTrim(null));
        assertEquals("", Utils.safeTrim(""));
        assertEquals("   ", Utils.safeTrim("   "));
    }
}