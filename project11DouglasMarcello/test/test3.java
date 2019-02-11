import java.util.Arrays;
import java.util.Scanner;
import java.lang.String;

class HelloWorld {
	// Read a String from the standard input using Scanner
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		String helloWorld = new String("HelloWorld\n");

		String byeWorld = new String("ByeWorld\n");
                System.out.println(helloWorld);

		while(true) {
			System.out.println("Enter your name: ");

    			// get their input as a String
    			String username = scanner.next();

    			// prompt for their age
   			System.out.print("Enter your age: \n");

    			// get the age as an int
    			int age = scanner.nextInt();

    			System.out.println(String.format("%s, your age is %d", username, age));
		}        
	}
} 