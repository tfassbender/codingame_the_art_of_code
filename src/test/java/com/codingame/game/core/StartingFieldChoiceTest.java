package com.codingame.game.core;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import com.codingame.game.build.RandomUtil;
import com.codingame.game.util.Pair;
import com.codingame.game.util.TestUtils;

public class StartingFieldChoiceTest {
	
	private StartingFieldChoice startingFieldChoice;
	
	@BeforeEach
	public void setup() {
		Random random = new Random();
		RandomUtil.init(random.nextLong());
		startingFieldChoice = new StartingFieldChoice(20);
	}
	
	@RepeatedTest(20)
	public void test_chose_random_starting_fields__no_duplicated_starting_fields() throws Exception {
		List<Pair<Integer, Integer>> startingFields = TestUtils.getFieldPerReflection(startingFieldChoice, "randomStartingFields");
		List<Integer> flattenedStartingFields = startingFields.stream().flatMap(pair -> Stream.of(pair.getKey(), pair.getValue())).collect(Collectors.toList());
		
		for (int i = 0; i < flattenedStartingFields.size(); i++) {
			for (int j = i + 1; j < flattenedStartingFields.size(); j++) {
				if (flattenedStartingFields.get(i) == flattenedStartingFields.get(j)) {
					fail("The starting field " + flattenedStartingFields.get(i) + " was chosen twice.");
				}
			}
		}
	}
}
