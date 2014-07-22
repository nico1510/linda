ejb:
	mkdir ./business
	cp /home/nico/NetBeansProjects/lindaejb/lindaejb-ejb/build/classes/business/ContentService.class ./business
	cp /home/nico/NetBeansProjects/lindaejb/lindaejb-ejb/build/classes/business/JobControlService.class ./business
	cp /home/nico/NetBeansProjects/lindaejb/lindaejb-ejb/build/classes/business/ToolLauncherBeanService.class ./business
	cp /home/nico/NetBeansProjects/lindaejb/lindaejb-ejb/build/classes/business/RepositoryService.class ./business

	jar cf lindaejb-ejb.jar ./business/ContentService.class ./business/JobControlService.class ./business/ToolLauncherBeanService.class ./business/RepositoryService.class

	rm -r ./business

	mkdir -p ./WEB-INF/lib
	mv ./lindaejb-ejb.jar ./WEB-INF/lib
	jar uf /home/nico/NetBeansProjects/lindaejb/lindaejb-war/dist/lindaejb-war.war ./WEB-INF/lib/lindaejb-ejb.jar
	rm -r ./WEB-INF

