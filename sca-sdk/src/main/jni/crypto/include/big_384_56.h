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
 * @file big_384_56.h
 * @author Mike Scott
 * @brief BIG Header File
 *
 */

#ifndef BIG_384_56_H
#define BIG_384_56_H

#include <stdio.h>
#include <stdlib.h>
//#include <inttypes.h>
#include "arch.h"
#include "amcl.h"
#include "config_big_384_56.h"

//#define UNWOUND

#define BIGBITS_384_56 (8*MODBYTES_384_56) /**< Length in bits */
#define NLEN_384_56 (1+((8*MODBYTES_384_56-1)/BASEBITS_384_56)) /**< length in bytes */
#define DNLEN_384_56 2*NLEN_384_56 /**< Double length in bytes */
#define BMASK_384_56 (((chunk)1<<BASEBITS_384_56)-1) /**< Mask = 2^BASEBITS-1 */
#define NEXCESS_384_56 (1<<(CHUNK-BASEBITS_384_56-1))           /**< 2^(CHUNK-BASEBITS-1) - digit cannot be multiplied by more than this before normalisation */

#define HBITS_384_56 (BASEBITS_384_56/2)      /**< Number of bits in number base divided by 2 */
#define HMASK_384_56 (((chunk)1<<HBITS_384_56)-1)    /**< Mask = 2^HBITS-1 */

//#define DEBUG_NORM

#ifdef DEBUG_NORM  /* Add an extra location to track chunk extension */
#define MPV_384_56 NLEN_384_56
#define MNV_384_56 (NLEN_384_56+1)
typedef chunk BIG_384_56[NLEN_384_56+2];   /**< Define type BIG as array of chunks */
#define DMPV_384_56 DNLEN_384_56
#define DMNV_384_56 (DNLEN_384_56+1)
typedef chunk DBIG_384_56[DNLEN_384_56+2]; /**< Define type DBIG as array of chunks */
#else
typedef chunk BIG_384_56[NLEN_384_56];     /**< Define type BIG as array of chunks */
typedef chunk DBIG_384_56[DNLEN_384_56];   /**< Define type DBIG as array of chunks */
#endif

/* BIG number prototypes */

/**	@brief Tests for BIG equal to zero
 *
	@param x a BIG number
	@return 1 if zero, else returns 0
 */
extern int BIG_384_56_iszilch(BIG_384_56 x);
/**	@brief Tests for BIG equal to one
 *
	@param x a BIG number
	@return 1 if one, else returns 0
 */
extern int BIG_384_56_isunity(BIG_384_56 x);
/**	@brief Tests for DBIG equal to zero
 *
	@param x a DBIG number
	@return 1 if zero, else returns 0
 */
extern int BIG_384_56_diszilch(DBIG_384_56 x);
/**	@brief Outputs a BIG number to the console
 *
	@param x a BIG number
 */
extern void BIG_384_56_output(BIG_384_56 x);
/**	@brief Outputs a BIG number to the console in raw form (for debugging)
 *
	@param x a BIG number
 */
extern void BIG_384_56_rawoutput(BIG_384_56 x);
/**	@brief Conditional constant time swap of two BIG numbers
 *
	Conditionally swaps parameters in constant time (without branching)
	@param x a BIG number
	@param y another BIG number
	@param s swap takes place if not equal to 0
 */
extern void BIG_384_56_cswap(BIG_384_56 x,BIG_384_56 y,int s);
/**	@brief Conditional copy of BIG number
 *
	Conditionally copies second parameter to the first (without branching)
	@param x a BIG number
	@param y another BIG number
	@param s copy takes place if not equal to 0
 */
extern void BIG_384_56_cmove(BIG_384_56 x,BIG_384_56 y,int s);
/**	@brief Conditional copy of DBIG number
 *
	Conditionally copies second parameter to the first (without branching)
	@param x a DBIG number
	@param y another DBIG number
	@param s copy takes place if not equal to 0
 */
extern void BIG_384_56_dcmove(BIG_384_56 x,BIG_384_56 y,int s);
/**	@brief Convert from BIG number to byte array
 *
	@param a byte array
	@param x BIG number
 */
extern void BIG_384_56_toBytes(char *a,BIG_384_56 x);
/**	@brief Convert to BIG number from byte array
 *
	@param x BIG number
	@param a byte array
 */
extern void BIG_384_56_fromBytes(BIG_384_56 x,char *a);
/**	@brief Convert to BIG number from byte array of given length
 *
	@param x BIG number
	@param a byte array
	@param s byte array length
 */
extern void BIG_384_56_fromBytesLen(BIG_384_56 x,char *a,int s);
/**@brief Convert to DBIG number from byte array of given length
 *
   @param x DBIG number
   @param a byte array
   @param s byte array length
 */
extern void BIG_384_56_dfromBytesLen(DBIG_384_56 x,char *a,int s);
/**	@brief Outputs a DBIG number to the console
 *
	@param x a DBIG number
 */
extern void BIG_384_56_doutput(DBIG_384_56 x);

/**	@brief Outputs a DBIG number to the console
 *
	@param x a DBIG number
 */
extern void BIG_384_56_drawoutput(DBIG_384_56 x);

/**	@brief Copy BIG from Read-Only Memory to a BIG
 *
	@param x BIG number
	@param y BIG number in ROM
 */
extern void BIG_384_56_rcopy(BIG_384_56 x,const BIG_384_56 y);
/**	@brief Copy BIG to another BIG
 *
	@param x BIG number
	@param y BIG number to be copied
 */
extern void BIG_384_56_copy(BIG_384_56 x,BIG_384_56 y);
/**	@brief Copy DBIG to another DBIG
 *
	@param x DBIG number
	@param y DBIG number to be copied
 */
extern void BIG_384_56_dcopy(DBIG_384_56 x,DBIG_384_56 y);
/**	@brief Copy BIG to upper half of DBIG
 *
	@param x DBIG number
	@param y BIG number to be copied
 */
extern void BIG_384_56_dsucopy(DBIG_384_56 x,BIG_384_56 y);
/**	@brief Copy BIG to lower half of DBIG
 *
	@param x DBIG number
	@param y BIG number to be copied
 */
extern void BIG_384_56_dscopy(DBIG_384_56 x,BIG_384_56 y);
/**	@brief Copy lower half of DBIG to a BIG
 *
	@param x BIG number
	@param y DBIG number to be copied
 */
extern void BIG_384_56_sdcopy(BIG_384_56 x,DBIG_384_56 y);
/**	@brief Copy upper half of DBIG to a BIG
 *
	@param x BIG number
	@param y DBIG number to be copied
 */
extern void BIG_384_56_sducopy(BIG_384_56 x,DBIG_384_56 y);
/**	@brief Set BIG to zero
 *
	@param x BIG number to be set to zero
 */
extern void BIG_384_56_zero(BIG_384_56 x);
/**	@brief Set DBIG to zero
 *
	@param x DBIG number to be set to zero
 */
extern void BIG_384_56_dzero(DBIG_384_56 x);
/**	@brief Set BIG to one (unity)
 *
	@param x BIG number to be set to one.
 */
extern void BIG_384_56_one(BIG_384_56 x);
/**	@brief Set BIG to inverse mod 2^256
 *
	@param x BIG number to be inverted
 */
extern void BIG_384_56_invmod2m(BIG_384_56 x);
/**	@brief Set BIG to sum of two BIGs - output not normalised
 *
	@param x BIG number, sum of other two
	@param y BIG number
	@param z BIG number
 */
extern void BIG_384_56_add(BIG_384_56 x,BIG_384_56 y,BIG_384_56 z);

/**	@brief Set BIG to logical or of two BIGs - output normalised
 *
	@param x BIG number, or of other two
	@param y BIG number
	@param z BIG number
 */
extern void BIG_384_56_or(BIG_384_56 x,BIG_384_56 y,BIG_384_56 z);

/**	@brief Increment BIG by a small integer - output not normalised
 *
	@param x BIG number to be incremented
	@param i integer
 */
extern void BIG_384_56_inc(BIG_384_56 x,int i);
/**	@brief Set BIG to difference of two BIGs
 *
	@param x BIG number, difference of other two - output not normalised
	@param y BIG number
	@param z BIG number
 */
extern void BIG_384_56_sub(BIG_384_56 x,BIG_384_56 y,BIG_384_56 z);
/**	@brief Decrement BIG by a small integer - output not normalised
 *
	@param x BIG number to be decremented
	@param i integer
 */
extern void BIG_384_56_dec(BIG_384_56 x,int i);
/**	@brief Set DBIG to sum of two DBIGs
 *
	@param x DBIG number, sum of other two - output not normalised
	@param y DBIG number
	@param z DBIG number
 */
extern void BIG_384_56_dadd(DBIG_384_56 x,DBIG_384_56 y,DBIG_384_56 z);
/**	@brief Set DBIG to difference of two DBIGs
 *
	@param x DBIG number, difference of other two - output not normalised
	@param y DBIG number
	@param z DBIG number
 */
extern void BIG_384_56_dsub(DBIG_384_56 x,DBIG_384_56 y,DBIG_384_56 z);
/**	@brief Multiply BIG by a small integer - output not normalised
 *
	@param x BIG number, product of other two
	@param y BIG number
	@param i small integer
 */
extern void BIG_384_56_imul(BIG_384_56 x,BIG_384_56 y,int i);
/**	@brief Multiply BIG by not-so-small small integer - output normalised
 *
	@param x BIG number, product of other two
	@param y BIG number
	@param i small integer
	@return Overflowing bits
 */
extern chunk BIG_384_56_pmul(BIG_384_56 x,BIG_384_56 y,int i);
/**	@brief Divide BIG by 3 - output normalised
 *
	@param x BIG number
	@return Remainder
 */
extern int BIG_384_56_div3(BIG_384_56 x);
/**	@brief Multiply BIG by even bigger small integer resulting in a DBIG - output normalised
 *
	@param x DBIG number, product of other two
	@param y BIG number
	@param i small integer
 */
extern void BIG_384_56_pxmul(DBIG_384_56 x,BIG_384_56 y,int i);
/**	@brief Multiply BIG by another BIG resulting in DBIG - inputs normalised and output normalised
 *
	@param x DBIG number, product of other two
	@param y BIG number
	@param z BIG number
 */
extern void BIG_384_56_mul(DBIG_384_56 x,BIG_384_56 y,BIG_384_56 z);
/**	@brief Multiply BIG by another BIG resulting in another BIG - inputs normalised and output normalised
 *
	Note that the product must fit into a BIG, and x must be distinct from y and z
	@param x BIG number, product of other two
	@param y BIG number
	@param z BIG number
 */
extern void BIG_384_56_smul(BIG_384_56 x,BIG_384_56 y,BIG_384_56 z);
/**	@brief Square BIG resulting in a DBIG - input normalised and output normalised
 *
	@param x DBIG number, square of a BIG
	@param y BIG number to be squared
 */
extern void BIG_384_56_sqr(DBIG_384_56 x,BIG_384_56 y);

/**	@brief Montgomery reduction of a DBIG to a BIG  - input normalised and output normalised
 *
	@param a BIG number, reduction of a BIG
	@param md BIG number, the modulus
	@param MC the Montgomery Constant
	@param d DBIG number to be reduced
 */
extern void BIG_384_56_monty(BIG_384_56 a,BIG_384_56 md,chunk MC,DBIG_384_56 d);

/**	@brief Shifts a BIG left by any number of bits - input must be normalised, output normalised
 *
	@param x BIG number to be shifted
	@param s Number of bits to shift
 */
extern void BIG_384_56_shl(BIG_384_56 x,int s);
/**	@brief Fast shifts a BIG left by a small number of bits - input must be normalised, output will be normalised
 *
	The number of bits to be shifted must be less than BASEBITS
	@param x BIG number to be shifted
	@param s Number of bits to shift
	@return Overflow bits
 */
extern int BIG_384_56_fshl(BIG_384_56 x,int s);
/**	@brief Shifts a DBIG left by any number of bits - input must be normalised, output normalised
 *
	@param x DBIG number to be shifted
	@param s Number of bits to shift
 */
extern void BIG_384_56_dshl(DBIG_384_56 x,int s);
/**	@brief Shifts a BIG right by any number of bits - input must be normalised, output normalised
 *
	@param x BIG number to be shifted
	@param s Number of bits to shift
 */
extern void BIG_384_56_shr(BIG_384_56 x,int s);
/**	@brief Fast shifts a BIG right by a small number of bits - input must be normalised, output will be normalised
 *
	The number of bits to be shifted must be less than BASEBITS
	@param x BIG number to be shifted
	@param s Number of bits to shift
	@return Shifted out bits
 */
extern int BIG_384_56_fshr(BIG_384_56 x,int s);
/**	@brief Shifts a DBIG right by any number of bits - input must be normalised, output normalised
 *
	@param x DBIG number to be shifted
	@param s Number of bits to shift
 */
extern void BIG_384_56_dshr(DBIG_384_56 x,int s);
/**	@brief Splits a DBIG into two BIGs - input must be normalised, outputs normalised
 *
	Internal function. The value of s must be approximately in the middle of the DBIG.
	Typically used to extract z mod 2^MODBITS and z/2^MODBITS
	@param x BIG number, top half of z
	@param y BIG number, bottom half of z
	@param z DBIG number to be split in two.
	@param s Bit position at which to split
	@return carry-out from top half
 */
extern chunk BIG_384_56_split(BIG_384_56 x,BIG_384_56 y,DBIG_384_56 z,int s);
/**	@brief Normalizes a BIG number - output normalised
 *
	All digits of the input BIG are reduced mod 2^BASEBITS
	@param x BIG number to be normalised
 */
extern chunk BIG_384_56_norm(BIG_384_56 x);
/**	@brief Normalizes a DBIG number - output normalised
 *
	All digits of the input DBIG are reduced mod 2^BASEBITS
	@param x DBIG number to be normalised
 */
extern void BIG_384_56_dnorm(DBIG_384_56 x);
/**	@brief Compares two BIG numbers. Inputs must be normalised externally
 *
	@param x first BIG number to be compared
	@param y second BIG number to be compared
	@return -1 is x<y, 0 if x=y, 1 if x>y
 */
extern int BIG_384_56_comp(BIG_384_56 x,BIG_384_56 y);
/**	@brief Compares two DBIG numbers. Inputs must be normalised externally
 *
	@param x first DBIG number to be compared
	@param y second DBIG number to be compared
	@return -1 is x<y, 0 if x=y, 1 if x>y
 */
extern int BIG_384_56_dcomp(DBIG_384_56 x,DBIG_384_56 y);
/**	@brief Calculate number of bits in a BIG - output normalised
 *
	@param x BIG number
	@return Number of bits in x
 */
extern int BIG_384_56_nbits(BIG_384_56 x);
/**	@brief Calculate number of bits in a DBIG - output normalised
 *
	@param x DBIG number
	@return Number of bits in x
 */
extern int BIG_384_56_dnbits(DBIG_384_56 x);
/**	@brief Reduce x mod n - input and output normalised
 *
	Slow but rarely used
	@param x BIG number to be reduced mod n
	@param n The modulus
 */
extern void BIG_384_56_mod(BIG_384_56 x,BIG_384_56 n);
/**	@brief Divide x by n - output normalised
 *
	Slow but rarely used
	@param x BIG number to be divided by n
	@param n The Divisor
 */
extern void BIG_384_56_sdiv(BIG_384_56 x,BIG_384_56 n);
/**	@brief  x=y mod n - output normalised
 *
	Slow but rarely used. y is destroyed.
	@param x BIG number, on exit = y mod n
	@param y DBIG number
	@param n Modulus
 */
extern void BIG_384_56_dmod(BIG_384_56 x,DBIG_384_56 y,BIG_384_56 n);
/**	@brief  x=y/n - output normalised
 *
	Slow but rarely used. y is destroyed.
	@param x BIG number, on exit = y/n
	@param y DBIG number
	@param n Modulus
 */
extern void BIG_384_56_ddiv(BIG_384_56 x,DBIG_384_56 y,BIG_384_56 n);
/**	@brief  return parity of BIG, that is the least significant bit
 *
	@param x BIG number
	@return 0 or 1
 */
extern int BIG_384_56_parity(BIG_384_56 x);
/**	@brief  return i-th of BIG
 *
	@param x BIG number
	@param i the bit of x to be returned
	@return 0 or 1
 */
extern int BIG_384_56_bit(BIG_384_56 x,int i);
/**	@brief  return least significant bits of a BIG
 *
	@param x BIG number
	@param n number of bits to return. Assumed to be less than BASEBITS.
	@return least significant n bits as an integer
 */
extern int BIG_384_56_lastbits(BIG_384_56 x,int n);
/**	@brief  Create a random BIG from a random number generator
 *
	Assumes that the random number generator has been suitably initialised
	@param x BIG number, on exit a random number
	@param r A pointer to a Cryptographically Secure Random Number Generator
 */
extern void BIG_384_56_random(BIG_384_56 x,csprng *r);
/**	@brief  Create an unbiased random BIG from a random number generator, reduced with respect to a modulus
 *
	Assumes that the random number generator has been suitably initialised
	@param x BIG number, on exit a random number
	@param n The modulus
	@param r A pointer to a Cryptographically Secure Random Number Generator
 */
extern void BIG_384_56_randomnum(BIG_384_56 x,BIG_384_56 n,csprng *r);
/**	@brief  Calculate x=y*z mod n
 *
	Slow method for modular multiplication
	@param x BIG number, on exit = y*z mod n
	@param y BIG number
	@param z BIG number
	@param n The BIG Modulus
 */
extern void BIG_384_56_modmul(BIG_384_56 x,BIG_384_56 y,BIG_384_56 z,BIG_384_56 n);
/**	@brief  Calculate x=y/z mod n
 *
	Slow method for modular division
	@param x BIG number, on exit = y/z mod n
	@param y BIG number
	@param z BIG number
	@param n The BIG Modulus
 */
extern void BIG_384_56_moddiv(BIG_384_56 x,BIG_384_56 y,BIG_384_56 z,BIG_384_56 n);
/**	@brief  Calculate x=y^2 mod n
 *
	Slow method for modular squaring
	@param x BIG number, on exit = y^2 mod n
	@param y BIG number
	@param n The BIG Modulus
 */
extern void BIG_384_56_modsqr(BIG_384_56 x,BIG_384_56 y,BIG_384_56 n);
/**	@brief  Calculate x=-y mod n
 *
	Modular negation
	@param x BIG number, on exit = -y mod n
	@param y BIG number
	@param n The BIG Modulus
 */
extern void BIG_384_56_modneg(BIG_384_56 x,BIG_384_56 y,BIG_384_56 n);
/**	@brief  Calculate jacobi Symbol (x/y)
 *
	@param x BIG number
	@param y BIG number
	@return Jacobi symbol, -1,0 or 1
 */
extern int BIG_384_56_jacobi(BIG_384_56 x,BIG_384_56 y);
/**	@brief  Calculate x=1/y mod n
 *
	Modular Inversion - This is slow. Uses binary method.
	@param x BIG number, on exit = 1/y mod n
	@param y BIG number
	@param n The BIG Modulus
 */
extern void BIG_384_56_invmodp(BIG_384_56 x,BIG_384_56 y,BIG_384_56 n);
/** @brief Calculate x=x mod 2^m
 *
	Truncation
	@param x BIG number, on reduced mod 2^m
	@param m new truncated size
*/
extern void BIG_384_56_mod2m(BIG_384_56 x,int m);

/**	@brief Calculates a*b+c+*d
 *
	Calculate partial product of a.b, add in carry c, and add total to d
	@param x multiplier
	@param y multiplicand
	@param c carry
	@param r pointer to accumulated bottom half of result
	@return top half of result
 */

#ifdef dchunk

/* Method required to calculate x*y+c+r, bottom half in r, top half returned */
static inline chunk muladd_384_56(chunk x,chunk y,chunk c,chunk *r)
{
    dchunk prod=(dchunk)x*y+c+*r;
    *r=(chunk)prod&BMASK_384_56;
    return (chunk)(prod>>BASEBITS_384_56);
}

#else

/* No integer type available that can store double the wordlength */
/* accumulate partial products */

static inline chunk muladd_384_56(chunk x,chunk y,chunk c,chunk *r)
{
    chunk x0,x1,y0,y1;
    chunk bot,top,mid,carry;
    x0=x&HMASK_384_56;
    x1=(x>>HBITS_384_56);
    y0=y&HMASK_384_56;
    y1=(y>>HBITS_384_56);
    bot=x0*y0;
    top=x1*y1;
    mid=x0*y1+x1*y0;
    x0=mid&HMASK_384_56;
    x1=(mid>>HBITS_384_56);
    bot+=x0<<HBITS_384_56;
    bot+=*r;
    bot+=c;

    top+=x1;
    carry=bot>>BASEBITS_384_56;
    bot&=BMASK_384_56;
    top+=carry;

    *r=bot;
    return top;
}

#endif


#endif
