package bitwiseAndStr;

public class URLIfy {

    public static void main(String[] args) {
	// replace space with %20
	String url = "my name is abhinav sunderrajan";
	int spaceCount = 0;
	for (char c : url.toCharArray())
	    if (c == ' ')
		spaceCount++;
	System.out.println(spaceCount);

	char c[] = new char[url.length() + spaceCount * 2];
	int j = 0;
	for (int i = 0; i < url.length(); i++) {
	    if (url.charAt(i) == ' ') {
		c[i + (j++)] = '%';
		c[i + (j++)] = '2';
		c[i + (j)] = '0';
	    } else {
		c[i + j] = url.charAt(i);
	    }
	}

	System.out.println(new String(c));

    }

}
