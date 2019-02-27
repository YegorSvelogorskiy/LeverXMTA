package com.leverx.leverxspringproj.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.leverx.leverxspringproj.domain.Destination;
import com.leverx.leverxspringproj.domain.Property;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sap.cloud.sdk.cloudplatform.CloudPlatform;
import com.sap.cloud.sdk.cloudplatform.ScpCfCloudPlatform;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.GenericDestination;
import com.sap.cloud.sdk.cloudplatform.security.AuthToken;
import com.sap.cloud.sdk.cloudplatform.security.AuthTokenFacade;

@Service
public class CloudService {

	@Autowired
	private CloudPlatform platform;

	@Autowired
	private ScpCfCloudPlatform platformScp;

	@Autowired
	private AuthTokenFacade authtoken;

	public String getApplicationName() {
		return platform.getApplicationName();
	}

	public Map<String, JsonElement> getSpaceName() {
		return platformScp.getVcapApplication();
	}

	public Map<String, JsonArray> getSchemaName() {
		return platformScp.getVcapServices();
	}

	public Optional<AuthToken> getCurrToken() {
		return authtoken.getCurrentToken();
	}
	public JsonObject getInfo(Optional<AuthToken> token) {
		String[] split_string = token.get().getJwt().getToken().split("\\.");
		String base64EncodedBody = split_string[1];
		Base64 base64Url = new Base64(true);
		String body = new String(base64Url.decode(base64EncodedBody));
		JsonParser jsonParser = new JsonParser();
		return (JsonObject)jsonParser.parse(body);
	}

	public List<Destination> getDestinations() {
		List<Destination> destinationList = new ArrayList<>();
		Map<String, GenericDestination> destinationMap = DestinationAccessor.getGenericDestinationsByName();
		destinationMap.forEach((key, value) -> {
			Destination destination = new Destination();
			destination.setName(value.getName());
			destination.setDescription(value.getDescription().orElseGet(() -> "No description"));
			destination.setDestinationType(value.getDestinationType().toString());
			Map<String, String> propertyMap = value.getPropertiesByName();
			List<Property> propertyList = new ArrayList<>();
			propertyMap.forEach((name, data) -> {
				Property property = new Property();
				property.setName(name);
				property.setValue(data);
				propertyList.add(property);
			});
			destination.setPropertyList(propertyList);
			destinationList.add(destination);
		});
		return destinationList;
	}

}
