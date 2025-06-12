/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/03/2025
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;

import java.math.BigDecimal;

public class V_ammortamento_beni_detBulk extends V_ammortamento_beni_detBase {


	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_AMMORTAMENTO_BENI_DET
	 **/
	public V_ammortamento_beni_detBulk() {
		super();
	}

	private BigDecimal imponibileAmmortamentoBene;
	private BigDecimal valoreAmmortizzatoBene;
	private BigDecimal imponibileAmmortamentoCalcolato;
	private BigDecimal valoreAmmortizzatoCalcolato;

	public BigDecimal getImponibileAmmortamentoCalcolato() {
		return imponibileAmmortamentoCalcolato;
	}

	public void setImponibileAmmortamentoCalcolato(BigDecimal imponibileAmmortamentoCalcolato) {
		this.imponibileAmmortamentoCalcolato = imponibileAmmortamentoCalcolato;
	}

	public BigDecimal getValoreAmmortizzatoCalcolato() {
		return valoreAmmortizzatoCalcolato;
	}

	public void setValoreAmmortizzatoCalcolato(BigDecimal valoreAmmortizzatoCalcolato) {
		this.valoreAmmortizzatoCalcolato = valoreAmmortizzatoCalcolato;
	}

	public BigDecimal getImponibileAmmortamentoBene() {
		return imponibileAmmortamentoBene;
	}

	public void setImponibileAmmortamentoBene(BigDecimal imponibileAmmortamentoBene) {
		this.imponibileAmmortamentoBene = imponibileAmmortamentoBene;
	}

	public BigDecimal getValoreAmmortizzatoBene() {
		return valoreAmmortizzatoBene;
	}

	public void setValoreAmmortizzatoBene(BigDecimal valoreAmmortizzatoBene) {
		this.valoreAmmortizzatoBene = valoreAmmortizzatoBene;
	}
}