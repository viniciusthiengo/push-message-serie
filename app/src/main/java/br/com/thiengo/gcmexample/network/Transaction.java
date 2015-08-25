package br.com.thiengo.gcmexample.network;

import org.json.JSONObject;

import br.com.thiengo.gcmexample.domain.WrapObjToNetwork;

/**
 * Created by viniciusthiengo on 7/26/15.
 */
public interface Transaction {
    WrapObjToNetwork doBefore();

    void doAfter(JSONObject jsonObject);
}
