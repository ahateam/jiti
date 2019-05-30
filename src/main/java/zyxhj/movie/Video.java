package zyxhj.movie;


import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 *  视频
 *
 */
@RDSAnnEntity(alias = "tb_video")
public class Video {

	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;
	
	@RDSAnnField(column = RDSAnnField.VARCHAR)
	public String title;
	
	@RDSAnnField(column = RDSAnnField.VARCHAR)
	public String type;
	
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String imageUrl;
	
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte urlType;
	
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String videoUrl;
	
}
