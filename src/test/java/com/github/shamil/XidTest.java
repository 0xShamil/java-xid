package com.github.shamil;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class XidTest {

    @Test
    public void testToBytes() {
        byte[] expectedBytes = new byte[]{81, 6, -4, -102, -68, -126, 55, 85, -127, 54, -46, -119};
        Xid id = new Xid(expectedBytes);

        assertArrayEquals(expectedBytes, id.toByteArray());

        ByteBuffer buffer = ByteBuffer.allocate(12);
        id.putToByteBuffer(buffer);
        assertArrayEquals(expectedBytes, buffer.array());
    }

    @Test
    public void testFromBytes() {
        byte[] bytes = new byte[]{81, 6, -4, -102, -68, -126, 55, 85, -127, 54, -46, -119};

        Xid xid1 = new Xid(bytes);
        assertEquals(0x5106FC9A, xid1.getTimestamp());

        Xid xid2 = new Xid(ByteBuffer.wrap(bytes));
        assertEquals(0x5106FC9A, xid2.getTimestamp());
    }

    @Test
    public void testLengthValidation() {
        Throwable whenNull = assertThrows(
                IllegalArgumentException.class,
                () -> new Xid((byte[]) null)
        );
        assertEquals("bytes can not be null", whenNull.getMessage());

        Throwable whenLengthIsLessThanExpected = assertThrows(
                IllegalArgumentException.class,
                () -> new Xid(new byte[11])
        );
        assertEquals("state should be: bytes has length of 12", whenLengthIsLessThanExpected.getMessage());

        Throwable whenLengthIsMoreThanExpected = assertThrows(
                IllegalArgumentException.class,
                () -> new Xid(new byte[13])
        );
        assertEquals("state should be: bytes has length of 12", whenLengthIsMoreThanExpected.getMessage());
    }

    @Test
    public void testBytesRoundTrip() {
        Xid expected = new Xid();
        Xid actual = new Xid(expected.toByteArray());
        assertEquals(expected, actual);

        byte[] b = new byte[12];
        Random r = new Random(17);
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (r.nextInt());
        }
        expected = new Xid(b);
        assertEquals(expected, new Xid(expected.toByteArray()));
    }

    @Test
    public void testGetSmallestWithDate() {
        Date date = new Date(1588467737760L);
        byte[] expectedBytes = new byte[]{94, -82, 24, 25, 0, 0, 0, 0, 0, 0, 0, 0};
        Xid xid = Xid.getSmallestWithDate(date);
        assertArrayEquals(expectedBytes, xid.toByteArray());
        assertEquals(date.getTime() / 1000 * 1000, xid.getDate().getTime());
        assertEquals(-1, xid.compareTo(new Xid(date)));
    }

    @Test
    public void testGetTimeZero() {
        assertEquals(0L, new Xid(0, 0).getDate().getTime());
    }

    @Test
    public void testGetTimeMaxSignedInt() {
        assertEquals(0x7FFFFFFFL * 1000, new Xid(0x7FFFFFFF, 0).getDate().getTime());
    }

    @Test
    public void testGetTimeMaxSignedIntPlusOne() {
        assertEquals(0x80000000L * 1000, new Xid(0x80000000, 0).getDate().getTime());
    }

    @Test
    public void testGetTimeMaxInt() {
        assertEquals(0xFFFFFFFFL * 1000, new Xid(0xFFFFFFFF, 0).getDate().getTime());
    }

    @Test
    public void testTime() {
        long a = System.currentTimeMillis();
        long b = (new Xid()).getDate().getTime();
        assertTrue(Math.abs(b - a) < 3000);
    }

    @Test
    public void testDateCons() {
        assertEquals(new Date().getTime() / 1000, new Xid(new Date()).getDate().getTime() / 1000);
    }

    @Test
    public void testHexStringConstructor() {
        Xid id = new Xid();
        assertEquals(id, new Xid(id.toHexString()));
    }

    @Test
    public void testCompareTo() {
        Date dateOne = new Date();
        Date dateTwo = new Date(dateOne.getTime() + 10000);
        Xid first = new Xid(dateOne, 0);
        Xid second = new Xid(dateOne, 1);
        Xid third = new Xid(dateTwo, 0);
        assertEquals(0, first.compareTo(first));
        assertEquals(-1, first.compareTo(second));
        assertEquals(-1, first.compareTo(third));
        assertEquals(1, second.compareTo(first));
        assertEquals(1, third.compareTo(first));
    }

    @Test
    public void testToHexString() {
        assertEquals("00000000000000000000", new Xid(new byte[12]).toHexString());
        assertEquals("9m4e2mr0ui3e8a215n4g",
                new Xid(new byte[]{(byte) 0x4d, (byte) 0x88, (byte) 0xe1, (byte) 0x5b, (byte) 0x60, (byte) 0xf4, (byte) 0x86, (byte) 0xe4, (byte) 0x28, (byte) 0x41, (byte) 0x2d, (byte) 0xc9}).toHexString());
    }

    @Test
    public void testFromHexString() {
        byte[] actual = new Xid("9m4e2mr0ui3e8a215n4g").toByteArray();
        byte[] expected = new byte[]{(byte) 0x4d, (byte) 0x88, (byte) 0xe1, (byte) 0x5b, (byte) 0x60, (byte) 0xf4, (byte) 0x86, (byte) 0xe4, (byte) 0x28, (byte) 0x41, (byte) 0x2d, (byte) 0xc9};
        assertArrayEquals(expected, actual);
    }

    private Date getDate(final String s) throws ParseException {
        return new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss Z").parse(s);
    }

    @Test
    public void testTimeZero() throws ParseException {
        assertEquals(getDate("01-Jan-1970 00:00:00 -0000"), new Xid(0, 0).getDate());
    }

    @Test
    public void testTimeMaxSignedInt() throws ParseException {
        assertEquals(getDate("19-Jan-2038 03:14:07 -0000"), new Xid(0x7FFFFFFF, 0).getDate());
    }

    @Test
    public void testTimeMaxSignedIntPlusOne() throws ParseException {
        assertEquals(getDate("19-Jan-2038 03:14:08 -0000"), new Xid(0x80000000, 0).getDate());
    }

    @Test
    public void testTimeMaxInt() throws ParseException {
        assertEquals(getDate("07-Feb-2106 06:28:15 -0000"), new Xid(0xFFFFFFFF, 0).getDate());
    }

    @Test
    void testIntervals() {
        List<Xid> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ids.add(Xid.get());
        }

        for (int i = 1; i < 10; i++) {
            Xid previousId = ids.get(i - 1);
            Xid currentId = ids.get(i);

            int diffSecs = currentId.getTimestamp() - previousId.getTimestamp();
            // test that both ids generated within same second
            assertEquals(0, diffSecs);

            // test currentId is greater than the previous
            assertTrue(currentId.compareTo(previousId) > 0);
        }
    }

    @Test
    public void testNoCollisions() {
        assertTrue(hasNoCollisions(1000000));
    }

    private boolean hasNoCollisions(int iterations) {
        Map<String, String> ids = new HashMap<>();
        for (int i = 0; i < iterations; i++) {
            String id = Xid.string();
            if (ids.containsKey(id)) {
                return false;
            } else {
                ids.put(id, id);
            }
        }
        return true;
    }

}