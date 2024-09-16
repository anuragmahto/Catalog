import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

public class RevisedSecretKeyFinder {

    // Helper method to decode a number from any base to base 10
    public static BigInteger decodeValue(String value, int base) {
        return new BigInteger(value, base);
    }

    // Lagrange interpolation to find the secret (y-intercept)
    public static BigInteger lagrangeInterpolation(BigInteger[] x, BigInteger[] y) {
        BigInteger secret = BigInteger.ZERO;
        BigInteger p = new BigInteger("340282366920938463463374607431768211507"); // Prime modulus (2^128 - 159)

        for (int i = 0; i < x.length; i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < x.length; j++) {
                if (i != j) {
                    numerator = numerator.multiply(x[j].negate()).mod(p);
                    denominator = denominator.multiply(x[i].subtract(x[j])).mod(p);
                }
            }

            BigInteger lagrangeTerm = y[i].multiply(numerator).multiply(denominator.modInverse(p));
            secret = secret.add(lagrangeTerm).mod(p);
        }

        return secret;
    }

    public static void main(String[] args) {
       String jsonData = "{\n" +
                "    \"keys\": {\n" +
                "        \"n\": 4,\n" +
                "        \"k\": 3\n" +
                "    },\n" +
                "    \"1\": {\n" +
                "        \"base\": \"10\",\n" +
                "        \"value\": \"4\"\n" +
                "    },\n" +
                "    \"2\": {\n" +
                "        \"base\": \"2\",\n" +
                "        \"value\": \"111\"\n" +
                "    },\n" +
                "    \"3\": {\n" +
                "        \"base\": \"10\",\n" +
                "        \"value\": \"12\"\n" +
                "    },\n" +
                "    \"6\": {\n" +
                "        \"base\": \"4\",\n" +
                "        \"value\": \"213\"\n" +
                "    }\n" +
                "}";

        // Parse the JSON string into a JSONObject
        JSONObject jsonObject = new JSONObject(jsonData);

        // Extract the number of points (n) and threshold (k)
        int n = jsonObject.getJSONObject("keys").getInt("n");
        int k = jsonObject.getJSONObject("keys").getInt("k");

        // Extract points from the JSON
        List<BigInteger> xValues = new ArrayList<>();
        List<BigInteger> yValues = new ArrayList<>();

        for (String key : jsonObject.keySet()) {
            if (!key.equals("keys")) {
                BigInteger x = new BigInteger(key);
                JSONObject point = jsonObject.getJSONObject(key);
                int base = Integer.parseInt(point.getString("base"));
                String value = point.getString("value");
                BigInteger y = decodeValue(value, base);

                xValues.add(x);
                yValues.add(y);

                System.out.println("Point " + x + ": " + y); // Debug output
            }
        }

        // Check if we have enough points
        if (xValues.size() < k) {
            System.out.println("Error: Not enough points for interpolation. Need at least " + k + " points.");
            return;
        }

        // Convert Lists to arrays
        BigInteger[] x = xValues.toArray(new BigInteger[0]);
        BigInteger[] y = yValues.toArray(new BigInteger[0]);

        // Perform Lagrange interpolation to find the secret
        BigInteger secret = lagrangeInterpolation(x, y);

        // Output the secret key
        System.out.println("The secret key is: " + secret);
    }
}