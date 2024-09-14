package it.cnr.contab.web.rest.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.cnr.contab.anagraf00.core.bulk.BancaBulk;

import java.util.Optional;
@JsonPropertyOrder({"pgBanca","intestazione","abi","cab","numeroConto","iban","swiftCode"})
public class DettaglioModalitaPagDto {

    private BancaBulk bancaBulk;

    public DettaglioModalitaPagDto(BancaBulk bancaBulk) {
        this.bancaBulk = bancaBulk;
    }



    public Long getPgBanca(){
        return Optional.ofNullable(bancaBulk).map(s-> s.getPg_banca()).orElse(Long.valueOf(0));
    }
    public String getAbi(){
        return Optional.ofNullable(bancaBulk).map(s-> s.getAbi()).orElse("");
    }
    public String getCab(){
        return Optional.ofNullable(bancaBulk).map(s-> s.getCab()).orElse("");
    }
    public String getNumeroConto(){
        return Optional.ofNullable(bancaBulk).map(s-> s.getNumero_conto()).orElse("");
    }
    public String getIntestazione(){
        return Optional.ofNullable(bancaBulk).map(s-> s.getIntestazione()).orElse("");
    }
    public String getIban(){
        return Optional.ofNullable(bancaBulk).map(s-> s.getCodice_iban()).orElse("");
    }
    public String getSwiftCode(){
        return Optional.ofNullable(bancaBulk).map(s-> s.getCodice_swift()).orElse("");
    }



}
