package org.opsli.modulars.system.dict.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.opsli.api.wrapper.system.dict.SysDictDetailModel;
import org.opsli.api.wrapper.system.dict.SysDictModel;
import org.opsli.common.exception.ServiceException;
import org.opsli.common.utils.WrapperUtil;
import org.opsli.core.base.service.impl.CrudServiceImpl;
import org.opsli.modulars.system.SystemMsg;
import org.opsli.modulars.system.dict.entity.SysDict;
import org.opsli.modulars.system.dict.mapper.DictMapper;
import org.opsli.modulars.system.dict.service.IDictDetailService;
import org.opsli.modulars.system.dict.service.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;


/**
 * @BelongsProject: opsli-boot
 * @BelongsPackage: org.opsli.modulars.test.service
 * @Author: Parker
 * @CreateTime: 2020-09-16 17:34
 * @Description: 数据字典 接口实现类
 */
@Service
public class DictServiceImpl extends CrudServiceImpl<DictMapper, SysDictModel, SysDict> implements IDictService {

    @Autowired(required = false)
    private DictMapper mapper;
    @Autowired
    private IDictDetailService iDictDetailService;

    @Override
    public SysDictModel insert(SysDictModel model) {
        if(model == null) return null;

        SysDict entity = WrapperUtil.transformInstance(model, SysDict.class);
        // 唯一验证
        Integer count = mapper.uniqueVerificationByCode(entity);
        if(count != null && count > 0){
            // 重复
            throw new ServiceException(SystemMsg.EXCEL_DICT_UNIQUE);
        }

        return super.insert(model);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysDictModel update(SysDictModel model) {
        if(model == null) return null;

        SysDict entity = WrapperUtil.transformInstance(model, SysDict.class);
        // 唯一验证
        Integer count = mapper.uniqueVerificationByCode(entity);
        if(count != null && count > 0){
            // 重复
            throw new ServiceException(SystemMsg.EXCEL_DICT_UNIQUE);
        }

        SysDictModel updateRet = super.update(model);

        // 字典主表修改 子表跟着联动 （验证是否改了编号）/ 或者修改不允许改编号
        List<SysDictDetailModel> listByTypeCode = null;
        if(StringUtils.isNotEmpty(model.getTypeCode())){
            listByTypeCode = iDictDetailService.findListByTypeCode(model.getTypeCode());
        }
        if(listByTypeCode != null && listByTypeCode.size() > 0){
            for (SysDictDetailModel sysDictDetailModel : listByTypeCode) {
                sysDictDetailModel.setTypeCode(updateRet.getTypeCode());
                iDictDetailService.update(sysDictDetailModel);
            }
        }

        return updateRet;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delete(String id) {
        // 删除字典明细表
        iDictDetailService.delByParent(id);

        // 删除自身数据
        return super.delete(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delete(SysDictModel model) {
        if(model == null || StringUtils.isEmpty(model.getId())){
            return false;
        }

        // 删除字典明细表
        iDictDetailService.delByParent(model.getId());

        // 删除自身数据
        return super.delete(model);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteAll(String[] ids) {
        if(ids == null) return false;

        // 删除字典明细表
        for (String id : ids) {
            iDictDetailService.delByParent(id);
        }
        // 删除自身数据
        return super.deleteAll(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteAll(Collection<SysDictModel> models) {
        if(models == null || models.isEmpty()) return false;

        // 删除字典明细表
        for (SysDictModel model : models) {
            if(model == null || StringUtils.isEmpty(model.getId())){
                continue;
            }
            iDictDetailService.delByParent(model.getId());
        }
        // 删除自身数据
        return super.deleteAll(models);
    }
}

