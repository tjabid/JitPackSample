/*
	Licensed to the Apache Software Foundation (ASF) under one
	or more contributor license agreements.  See the NOTICE file
	distributed with this work for additional information
	regarding copyright ownership.  The ASF licenses this file
	to you under the Apache License, Version 2.0 (the
	"License"); you may not use this file except in compliance
	with the License.  You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an
	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
	KIND, either express or implied.  See the License for the
	specific language governing permissions and limitations
	under the License.
*/

/**
 * @file ff_3072.h
 * @author Mike Scott
 * @brief FF Header File
 *
 */

#ifndef FF_3072_H
#define FF_3072_H

#include "big_384_56.h"
#include "config_ff_3072.h"

#define HFLEN_3072 (FFLEN_3072/2) /**< Useful for half-size RSA private key operations */
#define P_MBITS_3072 (MODBYTES_384_56*8) /**< Number of bits in modulus */
#define P_TBITS_3072 (P_MBITS_3072%BASEBITS_384_56) /**< TODO  */
#define P_EXCESS_3072(a) (((a[NLEN_384_56-1])>>(P_TBITS_3072))+1) /**< TODO */
#define P_FEXCESS_3072 ((chunk)1<<(BASEBITS_384_56*NLEN_384_56-P_MBITS_3072-1)) /**< TODO */


/* Finite Field Prototypes */
/**	@brief Copy one FF element of given length to another
 *
	@param x FF instance to be copied to, on exit = y
	@param y FF instance to be copied from
	@param n size of FF in BIGs

 */
extern void FF_3072_copy(BIG_384_56 *x,BIG_384_56 *y,int n);
/**	@brief Initialize an FF element of given length from a 32-bit integer m
 *
	@param x FF instance to be copied to, on exit = m
	@param m integer
	@param n size of FF in BIGs
 */
extern void FF_3072_init(BIG_384_56 *x,sign32 m,int n);
/**	@brief Set FF element of given size to zero
 *
	@param x FF instance to be set to zero
	@param n size of FF in BIGs
 */
extern void FF_3072_zero(BIG_384_56 *x,int n);
/**	@brief Tests for FF element equal to zero
 *
	@param x FF number to be tested
	@param n size of FF in BIGs
	@return 1 if zero, else returns 0
 */
extern int FF_3072_iszilch(BIG_384_56 *x,int n);
/**	@brief  return parity of an FF, that is the least significant bit
 *
	@param x FF number
	@return 0 or 1
 */
extern int FF_3072_parity(BIG_384_56 *x);
/**	@brief  return least significant m bits of an FF
 *
	@param x FF number
	@param m number of bits to return. Assumed to be less than BASEBITS.
	@return least significant n bits as an integer
 */
extern int FF_3072_lastbits(BIG_384_56 *x,int m);
/**	@brief Set FF element of given size to unity
 *
	@param x FF instance to be set to unity
	@param n size of FF in BIGs
 */
extern void FF_3072_one(BIG_384_56 *x,int n);
/**	@brief Compares two FF numbers. Inputs must be normalised externally
 *
	@param x first FF number to be compared
	@param y second FF number to be compared
	@param n size of FF in BIGs
	@return -1 is x<y, 0 if x=y, 1 if x>y
 */
extern int FF_3072_comp(BIG_384_56 *x,BIG_384_56 *y,int n);
/**	@brief addition of two FFs
 *
	@param x FF instance, on exit = y+z
	@param y FF instance
	@param z FF instance
	@param n size of FF in BIGs
 */
extern void FF_3072_add(BIG_384_56 *x,BIG_384_56 *y,BIG_384_56 *z,int n);
/**	@brief subtraction of two FFs
 *
	@param x FF instance, on exit = y-z
	@param y FF instance
	@param z FF instance
	@param n size of FF in BIGs
 */
extern void FF_3072_sub(BIG_384_56 *x,BIG_384_56 *y,BIG_384_56 *z,int n);
/**	@brief increment an FF by an integer,and normalise
 *
	@param x FF instance, on exit = x+m
	@param m an integer to be added to x
	@param n size of FF in BIGs
 */
extern void FF_3072_inc(BIG_384_56 *x,int m,int n);
/**	@brief Decrement an FF by an integer,and normalise
 *
	@param x FF instance, on exit = x-m
	@param m an integer to be subtracted from x
	@param n size of FF in BIGs
 */
extern void FF_3072_dec(BIG_384_56 *x,int m,int n);
/**	@brief Normalises the components of an FF
 *
	@param x FF instance to be normalised
	@param n size of FF in BIGs
 */
extern void FF_3072_norm(BIG_384_56 *x,int n);
/**	@brief Shift left an FF by 1 bit
 *
	@param x FF instance to be shifted left
	@param n size of FF in BIGs
 */
extern void FF_3072_shl(BIG_384_56 *x,int n);
/**	@brief Shift right an FF by 1 bit
 *
	@param x FF instance to be shifted right
	@param n size of FF in BIGs
 */
extern void FF_3072_shr(BIG_384_56 *x,int n);
/**	@brief Formats and outputs an FF to the console
 *
	@param x FF instance to be printed
	@param n size of FF in BIGs
 */
extern void FF_3072_output(BIG_384_56 *x,int n);
/**	@brief Formats and outputs an FF to the console, in raw form
 *
 	@param x FF instance to be printed
 	@param n size of FF in BIGs
 */
extern void FF_3072_rawoutput(BIG_384_56 *x,int n);
/**	@brief Formats and outputs an FF instance to an octet string
 *
	Converts an FF to big-endian base 256 form.
	@param S output octet string
	@param x FF instance to be converted to an octet string
	@param n size of FF in BIGs
 */
extern void FF_3072_toOctet(octet *S,BIG_384_56 *x,int n);
/**	@brief Populates an FF instance from an octet string
 *
	Creates FF from big-endian base 256 form.
	@param x FF instance to be created from an octet string
	@param S input octet string
	@param n size of FF in BIGs
 */
extern void FF_3072_fromOctet(BIG_384_56 *x,octet *S,int n);
/**	@brief Multiplication of two FFs
 *
	Uses Karatsuba method internally
	@param x FF instance, on exit = y*z
	@param y FF instance
	@param z FF instance
	@param n size of FF in BIGs
 */
extern void FF_3072_mul(BIG_384_56 *x,BIG_384_56 *y,BIG_384_56 *z,int n);
/**	@brief Reduce FF mod a modulus
 *
	This is slow
	@param x FF instance to be reduced mod m - on exit = x mod m
	@param m FF modulus
	@param n size of FF in BIGs
 */
extern void FF_3072_mod(BIG_384_56 *x,BIG_384_56 *m,int n);
/**	@brief Square an FF
 *
	Uses Karatsuba method internally
	@param x FF instance, on exit = y^2
	@param y FF instance to be squared
	@param n size of FF in BIGs
 */
extern void FF_3072_sqr(BIG_384_56 *x,BIG_384_56 *y,int n);
/**	@brief Reduces a double-length FF with respect to a given modulus
 *
	This is slow
	@param x FF instance, on exit = y mod z
	@param y FF instance, of double length 2*n
	@param z FF modulus
	@param n size of FF in BIGs
 */
extern void FF_3072_dmod(BIG_384_56 *x,BIG_384_56 *y,BIG_384_56 *z,int n);
/**	@brief Invert an FF mod a prime modulus
 *
	@param x FF instance, on exit = 1/y mod z
	@param y FF instance
	@param z FF prime modulus
	@param n size of FF in BIGs
 */
extern void FF_3072_invmodp(BIG_384_56 *x,BIG_384_56 *y,BIG_384_56 *z,int n);
/**	@brief Create an FF from a random number generator
 *
	@param x FF instance, on exit x is a random number of length n BIGs with most significant bit a 1
	@param R an instance of a Cryptographically Secure Random Number Generator
	@param n size of FF in BIGs
 */
extern void FF_3072_random(BIG_384_56 *x,csprng *R,int n);
/**	@brief Create a random FF less than a given modulus from a random number generator
 *
	@param x FF instance, on exit x is a random number < y
	@param y FF instance, the modulus
	@param R an instance of a Cryptographically Secure Random Number Generator
	@param n size of FF in BIGs
 */
extern void FF_3072_randomnum(BIG_384_56 *x,BIG_384_56 *y,csprng *R,int n);
/**	@brief Calculate r=x^e mod m, side channel resistant
 *
	@param r FF instance, on exit = x^e mod p
	@param x FF instance
	@param e FF exponent
	@param m FF modulus
	@param n size of FF in BIGs
 */
extern void FF_3072_skpow(BIG_384_56 *r,BIG_384_56 *x,BIG_384_56 * e,BIG_384_56 *m,int n);
/**	@brief Calculate r=x^e mod m, side channel resistant
 *
	For short BIG exponent
	@param r FF instance, on exit = x^e mod p
	@param x FF instance
	@param e BIG exponent
	@param m FF modulus
	@param n size of FF in BIGs
 */
extern void FF_3072_skspow(BIG_384_56 *r,BIG_384_56 *x,BIG_384_56 e,BIG_384_56 *m,int n);
/**	@brief Calculate r=x^e mod m
 *
	For very short integer exponent
	@param r FF instance, on exit = x^e mod p
	@param x FF instance
	@param e integer exponent
	@param m FF modulus
	@param n size of FF in BIGs
 */
extern void FF_3072_power(BIG_384_56 *r,BIG_384_56 *x,int e,BIG_384_56 *m,int n);
/**	@brief Calculate r=x^e mod m
 *
	@param r FF instance, on exit = x^e mod p
	@param x FF instance
	@param e FF exponent
	@param m FF modulus
	@param n size of FF in BIGs
 */
extern void FF_3072_pow(BIG_384_56 *r,BIG_384_56 *x,BIG_384_56 *e,BIG_384_56 *m,int n);
/**	@brief Test if an FF has factor in common with integer s
 *
	@param x FF instance to be tested
	@param s the supplied integer
	@param n size of FF in BIGs
	@return 1 if gcd(x,s)!=1, else return 0
 */
extern int FF_3072_cfactor(BIG_384_56 *x,sign32 s,int n);
/**	@brief Test if an FF is prime
 *
	Uses Miller-Rabin Method
	@param x FF instance to be tested
	@param R an instance of a Cryptographically Secure Random Number Generator
	@param n size of FF in BIGs
	@return 1 if x is (almost certainly) prime, else return 0
 */
extern int FF_3072_prime(BIG_384_56 *x,csprng *R,int n);
/**	@brief Calculate r=x^e.y^f mod m
 *
	@param r FF instance, on exit = x^e.y^f mod p
	@param x FF instance
	@param e BIG exponent
	@param y FF instance
	@param f BIG exponent
	@param m FF modulus
	@param n size of FF in BIGs
 */
extern void FF_3072_pow2(BIG_384_56 *r,BIG_384_56 *x,BIG_384_56 e,BIG_384_56 *y,BIG_384_56 f,BIG_384_56 *m,int n);

#endif
