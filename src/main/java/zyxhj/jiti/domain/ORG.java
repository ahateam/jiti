package zyxhj.jiti.domain;

import java.util.Date;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org")
public class ORG {

	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;

	/**
	 * 创建时间
	 */
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date createTime;

	/**
	 * 名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String name;

	/**
	 * 组织机构代码
	 */
	@RDSAnnField(column = "VARCHAR(64)")
	public String code;

	/**
	 * 省
	 */
	@RDSAnnField(column = "VARCHAR(32)")
	public String province;

	/**
	 * 市
	 */
	@RDSAnnField(column = "VARCHAR(32)")
	public String city;

	/**
	 * 区
	 */
	@RDSAnnField(column = "VARCHAR(32)")
	public String district;

	/**
	 * 具体地址
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_TITLE)
	public String address;

	/**
	 * 组织机构证书图片
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String imgOrg;

	/**
	 * 授权证书图片
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String imgAuth;

	/**
	 * 股份份数总数
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer shareAmount;

	/**
	 * 资金
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double capital;

	/**
	 * 负债
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double debt;

	/**
	 * 债权资金
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double receivables;

	/**
	 * 年毛收入
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double income;

	/**
	 * 分红
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double bonus;

	/**
	 * 预算
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double budget;

	/**
	 * 决算
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double financialBudget;

	/**
	 * 对外投资
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double investment;

	/**
	 * 估值
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double valuation;
}
