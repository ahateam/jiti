package zyxhj.jiti.repository;

import zyxhj.jiti.domain.PrintingTemplate;
import zyxhj.utils.data.rds.RDSRepository;

public class PrintingTemplateRepository extends RDSRepository<PrintingTemplate> {

	public PrintingTemplateRepository() {
		super(PrintingTemplate.class);
	}

}
