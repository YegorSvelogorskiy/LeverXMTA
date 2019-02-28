package com.leverx.leverxspringproj.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leverx.leverxspringproj.model.Destination;
import com.leverx.leverxspringproj.service.CloudService;
import com.leverx.leverxspringproj.service.SecurityService;
import com.sap.cloud.sdk.cloudplatform.security.AuthToken;
import com.sap.cloud.sdk.s4hana.connectivity.exception.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class HomeController {

	private static final String HANATRIAL = "hanatrial";
	private static final String CREDENTIALS = "credentials";
	private static final String SCHEMA = "schema";
	private static final String FINAL_NAME = "given_name";
	private static final String FAMILY_NAME = "family_name";
	private static final String SPACE_NAME = "space_name";


	@Autowired
	private CloudService cloudService;

	@Autowired
	private SecurityService securityService;

	@GetMapping(value="/")
	public String getHome(Model model) {
		Map<String, JsonElement> vcap = cloudService.getSpaceName();
		String appName = cloudService.getApplicationName();
		JsonElement vc = vcap.get(SPACE_NAME);
		JsonArray hanatrial = cloudService.getSchemaName().get(HANATRIAL);
		JsonElement schema = hanatrial.get(0).getAsJsonObject().get(CREDENTIALS).getAsJsonObject().get(SCHEMA);
		model.addAttribute("VCAP",vc.toString());
		model.addAttribute("appName", appName);
		model.addAttribute("schema", schema);
		List<Destination> destinations = cloudService.getDestinations();
		model.addAttribute("destinations", destinations);
		return "index";
	}

	@GetMapping(value="/jwt")
	public String getJWT(Model model) {
		Optional<AuthToken> token = cloudService.getCurrToken();
		JsonObject jo = cloudService.getInfo(token);
		JsonElement name = jo.get(FINAL_NAME);
		JsonElement familyname = jo.get(FAMILY_NAME);
		model.addAttribute("token", jo);
		model.addAttribute("name", name);
		model.addAttribute("familyname", familyname);
		return "jwt";
	}

	@GetMapping(value="/scope")
	public String checkScope() throws AccessDeniedException {
		securityService.userHasAuthorization("Display");
		return "scope";
	}
}