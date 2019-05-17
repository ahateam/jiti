package zyxhj.movie;

import zyxhj.utils.data.rds.RDSRepository;

public class VideoRepository extends RDSRepository<Video> {

	public VideoRepository() {
		super(Video.class);
	}

}
