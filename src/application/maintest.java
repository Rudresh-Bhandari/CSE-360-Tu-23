package application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class maintest {

	@Test
	// according to my logic and set up , the length of the code should always be 4 
	void codeLength() {
		Main main = new Main();
		String mfaCode = main.generateCode();
		assertEquals(4,mfaCode.length(),"The code length should be 4");
	}
	@Test
	void checkIfUniqe() {
		Main main = new Main();
		String first=main.generateCode();
		String second = main.generateCode();
		assertNotEquals(first,second,"The two codes should not be identical");
	}
	@Test 
	void codenumeric() {
		Main main = new Main();
		String code = main.generateCode();
		assertTrue(code.matches("\\d{4}"),"Code should be numeric");
	}

}
