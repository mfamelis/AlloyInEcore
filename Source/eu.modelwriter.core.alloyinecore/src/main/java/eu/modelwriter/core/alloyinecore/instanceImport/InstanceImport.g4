/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017, Ferhat Erata <ferhat@computer.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

grammar InstanceImport;

instance :
    .*? model (rootObject= eObject | ';')
    ;

model:
    ('model') (name= unrestrictedName ':')? ownedPathName= SINGLE_QUOTED_STRING
    ;

eObject:
    name= pathName id= value? ('{' slot+= content (',' slot+= content)* '}' | ';') ;

content:
    name= unrestrictedName (':' (dataValue | '{' eObject* '}' | objectReference))?;

dataValue: value | multiValueData;

multiValueData: '[' dataValue (',' dataValue)* ']' ;

objectReference: pathName | ('[' pathName (',' pathName)* ']');

pathName: firstSegment= unrestrictedName ('.' index= INT)? (midSegments+= segment*  lastSegment= segment)? ;

segment:'::' '@'? name= unrestrictedName ('.' index= INT)?;

value:
        identifier
    |   numericValue
    |   stringValue
    |   charValue
    |   booleanValue
    |   nullValue
    ;

unrestrictedName:
        identifier
    |   'import'
    |	'model'
;

booleanValue: 'true' | 'false';

numericValue: ('+' | '-')? INT? '.'? INT ;

identifier: IDENTIFIER;

stringValue: DOUBLE_QUOTED_STRING;

charValue: SINGLE_CHARACTER;

nullValue: 'null';

INT :   DIGIT+ ;

IDENTIFIER : (UNDERSCORE | LETTER) (LETTER | APOSTROPHE | DIGIT | UNDERSCORE | DOLLAR)* ;
SINGLE_CHARACTER: '\'' ~['\\] '\'';
DOUBLE_QUOTED_STRING: '"' ( ESCAPED_CHARACTER | ~('\\' | '"' ) )* '"'  ;
SINGLE_QUOTED_STRING: '\'' ( ESCAPED_CHARACTER | ~('\'' | '\\') )* '\'' ;


fragment LETTER: [a-zA-Z];
fragment DIGITS : DIGIT+ ;
fragment DIGIT: [0-9];
fragment ESCAPED_CHARACTER: '\\' ('b' | 't' | 'n' | 'f' | 'r' | 'u' | '"' | '\'' | '\\');
fragment UNDERSCORE: '_';
fragment APOSTROPHE: '\'';
fragment DOLLAR: '$';
fragment MINUS: '-';
ML_SINGLE_QUOTED_STRING : '\'' .*? '\'' -> skip;
MULTILINE_COMMENT : '/*' .*? '*/' -> skip;
SINGLELINE_COMMENT : '--' .*? '\r'? '\n' -> skip;

WS : [ \r\t\n]+ -> skip ;