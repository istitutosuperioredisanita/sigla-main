/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
* Created by Generator 1.0
* Date 30/08/2005
*/
package it.cnr.contab.config00.pdcep.cla.bulk;
import it.cnr.jada.persistency.Keyed;
public class Parametri_livelli_epBase extends Parametri_livelli_epKey implements Keyed {
//    LIVELLI NUMBER(2) NOT NULL
	private java.lang.Integer livelli;
 
//    LUNG_LIVELLO1 VARCHAR(1)
	private java.lang.Integer lung_livello1;
 
//    DS_LIVELLO1 VARCHAR(20)
	private java.lang.String ds_livello1;
 
//    LUNG_LIVELLO2 VARCHAR(1)
	private java.lang.Integer lung_livello2;
 
//    DS_LIVELLO2 VARCHAR(20)
	private java.lang.String ds_livello2;
 
//    LUNG_LIVELLO3 VARCHAR(1)
	private java.lang.Integer lung_livello3;
 
//    DS_LIVELLO3 VARCHAR(20)
	private java.lang.String ds_livello3;
 
//    LUNG_LIVELLO4 VARCHAR(1)
	private java.lang.Integer lung_livello4;
 
//    DS_LIVELLO4 VARCHAR(20)
	private java.lang.String ds_livello4;
 
//    LUNG_LIVELLO5 VARCHAR(1)
	private java.lang.Integer lung_livello5;
 
//    DS_LIVELLO5 VARCHAR(20)
	private java.lang.String ds_livello5;
 
//    LUNG_LIVELLO6 VARCHAR(1)
	private java.lang.Integer lung_livello6;
 
//    DS_LIVELLO6 VARCHAR(20)
	private java.lang.String ds_livello6;
 
//    LUNG_LIVELLO7 VARCHAR(1)
	private java.lang.Integer lung_livello7;
 
//    DS_LIVELLO7 VARCHAR(20)
	private java.lang.String ds_livello7;

//    LUNG_LIVELLO8 VARCHAR(1)
	private java.lang.Integer lung_livello8;
 
//    DS_LIVELLO8 VARCHAR(20)
	private java.lang.String ds_livello8;
 
	public Parametri_livelli_epBase() {
		super();
	}
	public Parametri_livelli_epBase(java.lang.Integer esercizio, String tipo) {
		super(esercizio,tipo);
	}

	public Integer getLivelli() {
		return livelli;
	}

	public void setLivelli(Integer livelli) {
		this.livelli = livelli;
	}

	public Integer getLung_livello1() {
		return lung_livello1;
	}

	public void setLung_livello1(Integer lung_livello1) {
		this.lung_livello1 = lung_livello1;
	}

	public String getDs_livello1() {
		return ds_livello1;
	}

	public void setDs_livello1(String ds_livello1) {
		this.ds_livello1 = ds_livello1;
	}

	public Integer getLung_livello2() {
		return lung_livello2;
	}

	public void setLung_livello2(Integer lung_livello2) {
		this.lung_livello2 = lung_livello2;
	}

	public String getDs_livello2() {
		return ds_livello2;
	}

	public void setDs_livello2(String ds_livello2) {
		this.ds_livello2 = ds_livello2;
	}

	public Integer getLung_livello3() {
		return lung_livello3;
	}

	public void setLung_livello3(Integer lung_livello3) {
		this.lung_livello3 = lung_livello3;
	}

	public String getDs_livello3() {
		return ds_livello3;
	}

	public void setDs_livello3(String ds_livello3) {
		this.ds_livello3 = ds_livello3;
	}

	public Integer getLung_livello4() {
		return lung_livello4;
	}

	public void setLung_livello4(Integer lung_livello4) {
		this.lung_livello4 = lung_livello4;
	}

	public String getDs_livello4() {
		return ds_livello4;
	}

	public void setDs_livello4(String ds_livello4) {
		this.ds_livello4 = ds_livello4;
	}

	public Integer getLung_livello5() {
		return lung_livello5;
	}

	public void setLung_livello5(Integer lung_livello5) {
		this.lung_livello5 = lung_livello5;
	}

	public String getDs_livello5() {
		return ds_livello5;
	}

	public void setDs_livello5(String ds_livello5) {
		this.ds_livello5 = ds_livello5;
	}

	public Integer getLung_livello6() {
		return lung_livello6;
	}

	public void setLung_livello6(Integer lung_livello6) {
		this.lung_livello6 = lung_livello6;
	}

	public String getDs_livello6() {
		return ds_livello6;
	}

	public void setDs_livello6(String ds_livello6) {
		this.ds_livello6 = ds_livello6;
	}

	public Integer getLung_livello7() {
		return lung_livello7;
	}

	public void setLung_livello7(Integer lung_livello7) {
		this.lung_livello7 = lung_livello7;
	}

	public String getDs_livello7() {
		return ds_livello7;
	}

	public void setDs_livello7(String ds_livello7) {
		this.ds_livello7 = ds_livello7;
	}

	public Integer getLung_livello8() {
		return lung_livello8;
	}

	public void setLung_livello8(Integer lung_livello8) {
		this.lung_livello8 = lung_livello8;
	}

	public String getDs_livello8() {
		return ds_livello8;
	}

	public void setDs_livello8(String ds_livello8) {
		this.ds_livello8 = ds_livello8;
	}
}