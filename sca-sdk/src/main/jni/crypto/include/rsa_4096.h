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
 * @file rsa_4096.h
 * @author Mike Scott
 * @brief RSA Header file for implementation of RSA protocol
 *
 * declares functions
 *
 */

#ifndef RSA_4096_H
#define RSA_4096_H

#include "ff_4096.h"
#include "rsa_support.h"

/*** START OF USER CONFIGURABLE SECTION -  ***/

#define HASH_TYPE_RSA_4096 SHA256 /**< Chosen Hash algorithm */

/*** END OF USER CONFIGURABLE SECTION ***/

#define RFS_4096 MODBYTES_512_60*FFLEN_4096 /**< RSA Public Key Size in bytes */


/**
	@brief Integer Factorisation Public Key
*/

typedef struct
{
    sign32 e;     /**< RSA exponent (typically 65537) */
    BIG_512_60 n[FFLEN_4096]; /**< An array of BIGs to store public key */
} rsa_public_key_4096;

/**
	@brief Integer Factorisation Private Key
*/

typedef struct
{
    BIG_512_60 p[FFLEN_4096/2];  /**< secret prime p  */
    BIG_512_60 q[FFLEN_4096/2];  /**< secret prime q  */
    BIG_512_60 dp[FFLEN_4096/2]; /**< decrypting exponent mod (p-1)  */
    BIG_512_60 dq[FFLEN_4096/2]; /**< decrypting exponent mod (q-1)  */
    BIG_512_60 c[FFLEN_4096/2];  /**< 1/p mod q */
} rsa_private_key_4096;

/* RSA Auxiliary Functions */

/**	@brief RSA Key Pair Generator
 *
	@param R is a pointer to a cryptographically secure random number generator
	@param e the encryption exponent
	@param PRIV the output RSA private key
	@param PUB the output RSA public key
        @param P Input prime number. Used when R is equal to NULL for testing
        @param Q Inpuy prime number. Used when R is equal to NULL for testing
 */
extern void RSA_4096_KEY_PAIR(csprng *R,sign32 e,rsa_private_key_4096* PRIV,rsa_public_key_4096* PUB,octet *P, octet* Q);

/**	@brief RSA encryption of suitably padded plaintext
 *
	@param PUB the input RSA public key
	@param F is input padded message
	@param G is the output ciphertext
 */
extern void RSA_4096_ENCRYPT(rsa_public_key_4096* PUB,octet *F,octet *G);
/**	@brief RSA decryption of ciphertext
 *
	@param PRIV the input RSA private key
	@param G is the input ciphertext
	@param F is output plaintext (requires unpadding)

 */
extern void RSA_4096_DECRYPT(rsa_private_key_4096* PRIV,octet *G,octet *F);
/**	@brief Destroy an RSA private Key
 *
	@param PRIV the input RSA private key. Destroyed on output.
 */
extern void RSA_4096_PRIVATE_KEY_KILL(rsa_private_key_4096 *PRIV);
/**	@brief Populates an RSA public key from an octet string
 *
	Creates RSA public key from big-endian base 256 form.
	@param x FF instance to be created from an octet string
	@param S input octet string
 */
extern void RSA_4096_fromOctet(BIG_512_60 *x,octet *S);



#endif
