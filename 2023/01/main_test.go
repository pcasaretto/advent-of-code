package main

import (
	"testing"
	"github.com/stretchr/testify/assert"
)

func TestFindDigits(t *testing.T) {
	tests := []struct {
		input string
		firstDigit int
		lastDigit int
	}{
		{"two1nine", 2, 9},
		{"eightwothree", 8, 3},
		{"abcone2threexyz", 1, 3},
		{"xtwone3four", 2, 4},
		{"4nineeightseven2", 4, 2 },
		{"zoneight234", 1, 4 },
		{"7pqrstsixteen", 7, 6 },
		{"248twofbkfpxtheightwovng", 2, 2},
		{"7pqrstsixteen", 7, 6 },
	}

	for _, test := range tests {
		firstDigit, lastDigit := findDigits(test.input)
		assert.Equal(t, test.firstDigit, firstDigit, "For input '%s' the first input should be %d", test.input, test.firstDigit)
		assert.Equal(t, test.lastDigit, lastDigit, "For input '%s', the last digit should be %d", test.input, test.lastDigit)
	}

}
