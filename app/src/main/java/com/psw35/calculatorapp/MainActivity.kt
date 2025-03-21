package com.psw35.calculatorapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    // initialize the working and result text views
    private lateinit var workingText : TextView
    private lateinit var resultsText : TextView

    // flags/bools for allowing operations and decimals at the appropriate times
    private var operationAllowed = false
    private var decimalAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // get the actual text views
        workingText = findViewById<TextView>(R.id.workingText)
        resultsText = findViewById<TextView>(R.id.resultsText)

        // for each button, get the id
        val buttonIDs = arrayOf(
            R.id.acButton, R.id.cButton, R.id.squareButton, R.id.sqrtButton,
            R.id.button7, R.id.button8, R.id.button9, R.id.divButton,
            R.id.button4, R.id.button5, R.id.button6, R.id.multButton,
            R.id.button1, R.id.button2, R.id.button3, R.id.subButton,
            R.id.button0, R.id.decButton, R.id.equalsButton, R.id.addButton
        )

        // for every button ID, get it's view and start an onClickListener
        for ( buttonID in buttonIDs ) {
            val button = findViewById<Button>(buttonID)
            button.setOnClickListener { onButtonClick(it) }
        }

    }

    // when a button is clicked
    private fun onButtonClick(view: View){
        // get the button and it's text
        val button = view as Button
        val buttonText = button.text.toString()

        // use the button text to figure out which button was clicked, and respond accordingly
        when ( buttonText ) {
            // if all clear
            "AC" -> {
                // reset text views
                workingText.text = ""
                resultsText.text = ""
            }
            // if clear
            "C" -> {
                // see if there's anything in workingText
                val length = workingText.length()
                // if there is, shorten the text by one char
                if ( length > 0 ) {
                    workingText.text = workingText.text.subSequence(0, length - 1)
                }
            }
            // if equals
            "=" -> {
                // evaulate normally
                resultsText.text = evaluate("")
            }
            // if square
            "x²" -> {
                // evaluate w/ squaring
                resultsText.text = evaluate("sqr")
            }
            // if square root
            "√" -> {
                // evaluate w/ square root
                resultsText.text = evaluate("sqrt")
            }
            // if any operator but minus sign
            "+", "x", "/" -> {
                // if we can do an operation right now
                if ( operationAllowed ) {
                    // get button operation and put it in workingText
                    workingText.append(buttonText)
                    // disallow it to be followed by another operation symbol
                    operationAllowed = false
                    // allow a decimal
                    decimalAllowed = true
                }
            }
            // if a minus sign
            "-" -> {
                // get working text length
                val length = workingText.length()
                // if not the first character, and a minus sign was already inputted
                // handle double negative:
                if ( workingText.length() > 0 && workingText.text.last() == '-' ) {
                    // if this is not at the beginning of working text
                    if ( workingText.length() > 1 ) {
                        // remove double negative
                        workingText.text = workingText.text.subSequence(0, length - 1)

                        // replace with a plus sign, if not done right before
                        if ( workingText.text.last() != '+' ) {
                            workingText.append("+")
                        }
                    }
                    // otherwise, just remove double negative/reset text
                    else {
                        workingText.text = ""
                    }
                }
                // else normal minus sign
                else {
                    // get button operation and put it in workingText
                    workingText.append(buttonText)
                    // disallow it to be followed by another operation symbol
                    operationAllowed = false
                    // allow a decimal
                    decimalAllowed = true
                }
            }
            // otherwise, is a digit or decimal
            else -> {
                // if a decimal
                if ( buttonText == "." ) {
                    // if allowed, add the decimal and prevent a subsequent decimal
                    if ( decimalAllowed ) {
                        workingText.append(buttonText)
                        decimalAllowed = false
                    }
                }
                // else is a digit
                else {
                    // get button number and put it in workingText
                    workingText.append(buttonText)
                    // allow an operation symbol after number
                    operationAllowed = true
                }
            }
        }
    }

    // evaluate function for when we want results
    private fun evaluate(specialOp: String): String {
        // separate double values and operators
        val sepExpression = separateExpression()

        // if there is text
        if ( sepExpression.isNotEmpty() ) {
            // do division and multiplication
            val multDiv = multDiv(sepExpression)

            // if no issues
            if ( multDiv.isNotEmpty() ) {
                // do addition and subtraction
                var result = addSub(multDiv)

                // when squaring or getting square root
                when ( specialOp ) {
                    // if squaring
                    "sqr" -> {
                        // multiply result by itself
                        result *= result
                    }
                    // if square root
                    "sqrt" -> {
                        // if user attempting a square root of a negative
                        if ( result < 0.00 ) {
                            // give a toast error and return error text
                            Toast.makeText(this, "Error: Sqrt of negative number!",
                                Toast.LENGTH_LONG).show()
                            return "Error"
                        }
                        // apply sqrt function on result
                        result = sqrt(result)
                    }
                }
                // if no special operation, return the results normally
                return result.toString()
            }
            // error text for if user attempts to divide by 0
            else {
                return "Error"
            }
        }

        // if user hit equals w/ an empty working text, send a toast message and return 0.00
        Toast.makeText(this, "Please enter something!", Toast.LENGTH_SHORT).show()
        return "0.00"
    }

    // addition and subtraction function
    private fun addSub(inputList: MutableList<Any>): Double {
        //
        var results = inputList[0] as Double

        for( index in inputList.indices ) {
            if ( inputList[index] is Char && index != inputList.lastIndex ) {
                val operator = inputList[index]
                val digitTwo = inputList[index + 1] as Double

                if ( operator == '+' ) {
                    results += digitTwo
                }
                else if ( operator == '-' ) {
                    results -= digitTwo
                }
            }
        }

        return results
    }

    // multiplaction and division function
    private fun multDiv(inputList: MutableList<Any>): MutableList<Any> {
        // save resultList as inputList in the case of no mult/div
        var resultsList = inputList

        // if there are multiply/divide symbols in the inputList
        while( resultsList.contains('x') || resultsList.contains('/') ) {
            // call helper on list
            resultsList = multDivHelper(resultsList)
        }

        // return list
        return resultsList
    }

    // helper function for multiplication and division
    private fun multDivHelper(inputList: MutableList<Any>): MutableList<Any> {
        // create a list to work with, and get the size of the list for iteration purposes
        var workingList = mutableListOf<Any>()
        var skipIndex = inputList.size

        // for element in the inputList
        for ( index in inputList.indices ) {
            // if a character and not at the end of input list,
            // while avoiding processing a number twice:
            if( inputList[index] is Char && index != inputList.lastIndex && index < skipIndex ) {
                // get digits and operator
                val digitOne = inputList[index - 1] as Double
                val operator = inputList[index]
                val digitTwo = inputList[index + 1] as Double

                // if multiplying
                if ( operator == 'x' ) {
                    // add product to working list
                    workingList.add(digitOne * digitTwo)
                    // update restartIndex to skip over next number
                    skipIndex = index + 1
                }
                // else if dividing
                else if ( operator == '/' ) {
                    // if one of the digits is 0
                    if ( digitOne == 0.00 || digitTwo == 0.00 ) {
                        // return a toast error and an empty list
                        Toast.makeText(this, "Error: Div by 0!", Toast.LENGTH_LONG).show()
                        workingList = mutableListOf<Any>()
                        return workingList
                    }
                    // otherwise, add quotient to working list
                    workingList.add(digitOne / digitTwo)
                    // update restartIndex to skip over next number
                    skipIndex = index + 1
                }
                // else is not related to mult/div, just add into working list as is
                else {
                    workingList.add(digitOne)
                    workingList.add(operator)
                }
            }

            // ensure we keep numbers not affected by the mult/div
            if ( index > skipIndex ) {
                workingList.add(inputList[index])
            }
        }

        // return the working list
        return workingList
    }

    // function to separate values and operators
    private fun separateExpression(): MutableList<Any> {
        // create a list for results
        val resultsList = mutableListOf<Any>()
        // initialize the double we will build for each value
        var resultDouble = ""
        // intialize a flag for negatives
        var negAllowed = true

        // for characters in working text
        for( charIndex in workingText.text ) {
            // if a digit or decimal
            if ( charIndex.isDigit() || charIndex == '.' ) {
                // add to the double
                resultDouble += charIndex
                // disallow negatives
                negAllowed = false
            }
            // else if a negative sign and not a minus sign
            else if ( charIndex == '-' && negAllowed ) {
                // add to the double
                resultDouble += charIndex
            }
            // else is an operator
            else {
                // add the double that was built to the list
                resultsList.add(resultDouble.toDouble())
                // reset the double holder
                resultDouble = ""
                // add operator to the list
                resultsList.add(charIndex)
                // allow negatives
                negAllowed = true
            }
        }

        // if anything left in the double holder
        if ( resultDouble.isNotEmpty() ) {
            // add it to the list
            resultsList.add(resultDouble.toDouble())
        }

        // return the list
        return resultsList
    }

}