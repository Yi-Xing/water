package top.fblue.framework.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 是否删除枚举
 */
@Getter
public enum DeletedEnum {
    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除");

    @EnumValue
    private final Integer code;
    private final String desc;

    DeletedEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static DeletedEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeletedEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        DeletedEnum state = fromCode(code);
        return state != null ? state.getDesc() : null;
    }

    /**
     * 检查code是否有效
     */
    public static boolean isValidCode(Integer code) {
        if (code == null) {
            return false;
        }
        for (DeletedEnum deletedEnum : values()) {
            if (deletedEnum.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
