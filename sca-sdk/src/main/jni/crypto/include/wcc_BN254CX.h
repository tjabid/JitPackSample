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
 * @file wcc_BN254CX.h
 * @author Mike Scott
 * @author Kealan McCusker
 * @brief WCC Header File
 *
 */

#ifndef WCC_BN254CX_H
#define WCC_BN254CX_H

#include "pair_BN254CX.h"
#include "pbc_support.h"

/* Field size is assumed to be greater than or equal to group size */

#define WCC_PGS_BN254CX MODBYTES_256_56          /**< WCC Group Size */
#define WCC_PFS_BN254CX MODBYTES_256_56          /**< WCC Field Size */

#define WCC_OK 0               /**< Function completed without error */
#define WCC_INVALID_POINT -51  /**< Point is NOT on the curve */
#define TIME_SLOT_MINUTES 1440 /**< Time Slot = 1 day */

#define PIV 12                 /**< AES-GCM Initialization Vector Size */
#define PTAG 16                /**< AES-GCM MAC Size */

/**
 * @brief Generate a random integer
 *
 * Generate a random number modulus the group order.
 *
 * @param  RNG    cryptographically secure random number generator
 * @param  S      Returned random integer modulus the group order
 */
int WCC_BN254CX_RANDOM_GENERATE(csprng *RNG,octet* S);

/**
 * @brief Hash EC Points and Id to an integer
 *
 * Perform sha256 of EC Points and Id. Map to an integer modulo the
 * curve order.
 *
 * <ol>
 * <li> x = toInteger(sha256(A,B,C,D))
 * <li> h = x % q where q is the curve order
 * </ol>
 *
 * @param  sha      Hash type
 * @param  A        EC Point
 * @param  B        EC Point
 * @param  C        EC Point
 * @param  D        Identity
 * @param  h        Integer result
 */
void WCC_BN254CX_Hq(int sha,octet *A,octet *B,octet *C,octet *D,octet *h);

/**
 * @brief Calculate value in G2 multiplied by an integer
 *
 * Calculate a value in G2. VG2 = s*H2(ID) where ID is the identity.
 *
 * <ol>
 * <li> VG2 = s*H2(ID)
 * </ol>
 *
 * @param  S         integer modulus curve order
 * @param  HID       Hash of ID padded with zeros to the field size
 * @param  VG2       EC Point VG2 = s*H2(ID)
 * @return rtn       Returns 0 if successful or else an error code
 */
int WCC_BN254CX_GET_G2_MULTIPLE(octet *S,octet *HID,octet *VG2);

/**
 * @brief Calculate value in G1 multiplied by an integer
 *
 * Calculate a value in G1. VG1 = s*H1(ID) where ID is the identity.
 *
 * <ol>
 * <li> VG1 = s*H1(ID)
 * </ol>
 *
 * @param  S           integer modulus curve order
 * @param  HID         Hash of ID padded with zeros to the field size
 * @param  VG1         EC point VG1 = s*H1(ID)
 * @return rtn         Returns 0 if successful or else an error code
 */
int WCC_BN254CX_GET_G1_MULTIPLE(octet *S,octet *HID,octet *VG1);

/**
 * @brief Calculate the sender AES key
 *
 * Calculate the sender AES Key.
 *
 * <ol>
 * <li> j=e((x+pia).AKeyG1,pib.BG2+PbG2)
 * <li> K=H(j,x.PgG1)
 * </ol>
 *
 * @param  sha         Hash type
 * @param  xOct        Random x < q where q is the curve order
 * @param  piaOct      Hq(PaG1,PbG2,PgG1)
 * @param  pibOct      Hq(PbG2,PaG1,PgG1)
 * @param  PbG2Oct     y.BG2 where y < q
 * @param  PgG1Oct     w.AG1 where w < q
 * @param  AKeyG1Oct   Sender key
 * @param  IdBOct      Receiver identity
 * @param  AESKeyOct   Returned AES key
 * @return rtn         Returns 0 if successful or else an error code
 */
int WCC_BN254CX_SENDER_KEY(int sha, octet *xOct, octet *piaOct, octet *pibOct, octet *PbG2Oct, octet *PgG1Oct, octet *AKeyG1Oct, octet *IdBOct, octet *AESKeyOct);

/**
 * @brief Calculate the receiver AES key
 *
 * Calculate the receiver AES key.
 *
 * <ol>
 * <li> j=e(pia.AG1+PaG1,(y+pib).BKeyG2)
 * <li> K=H(j,w.PaG1)
 * </ol>
 *
 * @param  sha         Hash type
 * @param  yOct        Random y < q where q is the curve order
 * @param  wOct        Random w < q where q is the curve order
 * @param  piaOct      Hq(PaG1,PbG2,PgG1)
 * @param  pibOct      Hq(PbG2,PaG1,PgG1)
 * @param  PaG1Oct     x.AG1 where x < q
 * @param  PgG1Oct     w.AG1 where w < q
 * @param  BKeyG2Oct   Receiver key
 * @param  IdAOct      Sender identity
 * @param  AESKeyOct   AES key returned
 * @return rtn         Returns 0 if successful or else an error code
 */
int WCC_BN254CX_RECEIVER_KEY(int sha, octet *yOct, octet *wOct,  octet *piaOct, octet *pibOct,  octet *PaG1Oct, octet *PgG1Oct, octet *BKeyG2Oct, octet *IdAOct, octet *AESKeyOct);

/**
 * @brief Add two members from the group G1
 *
 * Add two members from the group G1.
 *
 * @param  R1      member of G1
 * @param  R2      member of G1
 * @param  R       returns member of G1 = R1+R2
 * @return         Returns 0 if successful or else an error code
 */
int WCC_BN254CX_RECOMBINE_G1(octet *R1,octet *R2,octet *R);

/**
 * @brief Add two members from the group G2
 *
 * Add two members from the group G2.
 *
 * @param  W1      member of G2
 * @param  W2      member of G2
 * @param  W       returns member of G2 = W1+W2
 * @return         Returns 0 if successful or else an error code
 */
int WCC_BN254CX_RECOMBINE_G2(octet *W1,octet *W2,octet *W);

#endif
