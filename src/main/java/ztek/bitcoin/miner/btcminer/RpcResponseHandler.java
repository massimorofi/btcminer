package ztek.bitcoin.miner.btcminer;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.util.EntityUtils;

public class RpcResponseHandler extends BasicResponseHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.impl.client.BasicResponseHandler#handleEntity(org.apache.http
	 * .HttpEntity)
	 */
	@Override
	public String handleEntity(HttpEntity entity) throws IOException {
		// TODO Auto-generated method stub
		return super.handleEntity(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.impl.client.BasicResponseHandler#handleResponse(org.apache.
	 * http.HttpResponse)
	 */
	@Override
	public String handleResponse(HttpResponse response) throws HttpResponseException, IOException {
		final StatusLine statusLine = response.getStatusLine();
		final HttpEntity entity = response.getEntity();
		return entity == null ? null : handleEntity(entity);
	}

}
