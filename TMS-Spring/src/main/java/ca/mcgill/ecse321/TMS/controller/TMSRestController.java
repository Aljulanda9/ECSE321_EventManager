package ca.mcgill.ecse321.TMS.controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.TMS.dto.LocationTypeDto;

import com.google.common.collect.Lists;

import ca.mcgill.ecse321.TMS.dto.MunicipalityDto;
import ca.mcgill.ecse321.TMS.dto.SpeciesDto;
import ca.mcgill.ecse321.TMS.dto.TreeDto;

import ca.mcgill.ecse321.TMS.dto.TreeLocationDto;
import ca.mcgill.ecse321.TMS.dto.TreeStatusDto;
import ca.mcgill.ecse321.TMS.dto.UserDto;
import ca.mcgill.ecse321.TMS.model.Local;

import ca.mcgill.ecse321.TMS.model.LocationType;
import ca.mcgill.ecse321.TMS.model.LocationType.LandUseType;
import ca.mcgill.ecse321.TMS.model.Municipality;
import ca.mcgill.ecse321.TMS.model.Specialist;
import ca.mcgill.ecse321.TMS.model.Species;
import ca.mcgill.ecse321.TMS.model.Tree;
import ca.mcgill.ecse321.TMS.model.TreeLocation;
import ca.mcgill.ecse321.TMS.model.TreePLE;


import ca.mcgill.ecse321.TMS.model.TreeStatus;
import ca.mcgill.ecse321.TMS.model.User;
import ca.mcgill.ecse321.TMS.model.User.UserType;
import ca.mcgill.ecse321.TMS.model.UserRole;
import ca.mcgill.ecse321.TMS.model.TreeStatus;
import ca.mcgill.ecse321.TMS.model.User;


import ca.mcgill.ecse321.TMS.service.InvalidInputException;

import ca.mcgill.ecse321.TMS.service.TMSService;

@RestController
public class TMSRestController {

	@Autowired
	private TreePLE treePLE;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private TMSService service;
	
	@RequestMapping("/")
	public String index() {
		
		return "TreePLE application root. Use the REST API to manage trees.\n";
	}



	/*
	 * Here will have get and post requests
	 */

	
	//For now we'll force the Tree to be registered to already created status, species, user, municipality and locationtype
	@PostMapping(value = {"/trees/"})
	public TreeDto createTree(
			@RequestParam int height,
			@RequestParam int diameter, 
			@RequestParam Date datePlanted, 
			@RequestParam int x,
			@RequestParam int y, 
			@RequestParam String description) throws InvalidInputException {	
		
		TreeStatus testStatus;
		Species testSpecies;
		User testUser;
		Municipality testMunicipality;
		LocationType testType;
		
		if(treePLE.getStatuses().size()==0) {
			testStatus = treePLE.addStatus();
		}
		else {
			testStatus = treePLE.getStatus(0);
		}
		if(treePLE.getSpecies().size()==0) {
			testSpecies = treePLE.addSpecies("corn", 11, 12);
		}
		else {
			testSpecies = treePLE.getSpecies(0);
		}
		//if(treePLE.getUsers().size()==0) {
			testUser = treePLE.addUser("Karim");
			testUser.setUserType(UserType.Scientist);
		//}
		//else {
		//	testUser = treePLE.getUser(0);
		//}
		if(treePLE.getMunicipalities().size()==0) {
			testMunicipality = treePLE.addMunicipality(1, "McGill");
		}
		else {
			testMunicipality = treePLE.getMunicipality(0);
		}
		if(treePLE.getParks().size()==0) {
			testType = treePLE.addPark(1, "Mont Royal");
		}
		else {
			testType = treePLE.getPark(0);
		}
		testType.setLandUseType(LandUseType.Residential);
		
		Tree tree = service.createTree(height, diameter, datePlanted, testStatus, testSpecies, testUser, testMunicipality, x, y, description, testType);
		System.out.println(tree.getId());
		
		return convertToDto(tree);
	}

	
	@GetMapping(value = { "/trees", "/trees/" })
	public List<TreeDto> findAllTrees() {
		List<TreeDto> trees = Lists.newArrayList();
		//for (Tree tree : service.findAllTrees()) {
		//	trees.add(convertToDto(tree));
		//}
		return trees;
	}

	@PostMapping(value = { "/removeTree/{Id}", "/removeTree/{Id}/" })
	public TreeDto removeTree(@PathVariable ("Id") int id) throws InvalidInputException {
		// get tree by ID
		Tree t = service.getTreeById(id);
		TreeDto treeDto = convertToDto(t);
		service.removeTree(t);
		return treeDto;
	}

	// TODO Conversion methods

	private MunicipalityDto convertToDto(Municipality m) {
		return modelMapper.map(m, MunicipalityDto.class);
	}

	private TreeLocationDto convertToDto(TreeLocation tl) {
		TreeLocationDto tlDto = modelMapper.map(tl, TreeLocationDto.class);
		tlDto.setLocationType(convertToDto(tl.getLocationType()));
		return tlDto;
	}

	private LocationTypeDto convertToDto(LocationType lt) {
		return modelMapper.map(lt, LocationTypeDto.class);
	}

	private TreeStatusDto convertToDto(TreeStatus ts) {
		return modelMapper.map(ts, TreeStatusDto.class);
	}

	private SpeciesDto convertToDto(Species s) {
		return modelMapper.map(s, SpeciesDto.class);
	}

	private UserDto convertToDto(User user) {
		ArrayList<String> roles = new ArrayList<String>();
		UserDto usD = modelMapper.map(user, UserDto.class);
		for (UserRole role : user.getUserRoles()) {
			if (role instanceof Local) {
				roles.add("local");
			}
			if (role instanceof Specialist) {
				roles.add("specialist");
			}
		}
		usD.setRoles(roles);
		return usD;
	}
	


	private TreeDto convertToDto(Tree t) {
		TreeDto treeDto = modelMapper.map(t, TreeDto.class);
		treeDto.setLocation(convertToDto(t.getTreeLocation()));
		treeDto.setStatus(convertToDto(t.getTreeStatus()));
		treeDto.setSpecies(convertToDto(t.getSpecies()));
		treeDto.setUser(convertToDto(t.getLocal()));
		treeDto.setMunicipality(convertToDto(t.getMunicipality()));
		return treeDto;
	}

}
