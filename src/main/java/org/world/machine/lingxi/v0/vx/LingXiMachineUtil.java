package org.world.machine.lingxi.v0.vx;

import org.world.machine.lingxi.v0.vx.vm.rule.LingXiVmRule;
import org.world.machine.lingxi.v0.vx.vm.rule.LingXiVmRuleItem;
import org.world.machine.lingxi.v0.vx.vm.rule.LingXiVmRuleItemFrom;
import org.world.machine.lingxi.v0.vx.vm.rule.LingXiVmRuleItemTo;

import java.util.ArrayList;
import java.util.List;

/**
 * 灵犀图灵机 工具类
 */
public class LingXiMachineUtil {

    /**
     * 转换为逗号分割
     *
     * @param cells
     * @return
     */
    static String cellsToString(String[] cells) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < cells.length; i++) {
            if (i != 0) {
                buffer.append(",");
            }
            buffer.append(cells[i]);
        }
        return buffer.toString();
    }

    /**
     * 解析规则： [begin,end]itemFrom->itemTo;itemFrom->itemTo;
     *
     * @param rule
     * @return
     */
    public static List<LingXiVmRule> parseRule(String rule) {

        List<LingXiVmRule> vmRules = new ArrayList();

        String[] rules = rule.trim().split("\\[");
        for (int j = 0; j < rules.length; j++) {
            if ("".equals(rules[j])) {
                continue;
            }
            LingXiVmRule vmRule = new LingXiVmRule();

            String rangeRule = (rules[j].contains("]") ? "[" : "") + rules[j];

            int beginBeginIndex = rangeRule.indexOf('[');
            int endEndIndex = rangeRule.indexOf(']');

            if (beginBeginIndex > -1 && endEndIndex > -1) {
                String[] beginEnd = rangeRule.substring(beginBeginIndex + 1, endEndIndex).split("\\s*,\\s*");

                int ruleBeginIndex = Integer.valueOf(beginEnd[0]);
                int ruleEndIndex = beginEnd.length == 1 ? ruleBeginIndex : Integer.valueOf(beginEnd[1]);

                vmRule.setTapeBeginIndex(ruleBeginIndex);
                vmRule.setTapeEndIndex(ruleEndIndex);
            }

            String[] ruleItems = rangeRule.substring(endEndIndex + 1).split("\\s*;\\s*");

            for (int i = 0; i < ruleItems.length; i++) {
                String[] ruleItem = ruleItems[i].trim().split("\\s*(,|(->))\\s*", -1);
                LingXiVmRuleItem vmRuleItem = new LingXiVmRuleItem();
                vmRuleItem.setFrom(new LingXiVmRuleItemFrom(ruleItem[0], ruleItem[1]));
                vmRuleItem.setTo(new LingXiVmRuleItemTo(ruleItem[2], ruleItem[3], ruleItem[4]));
                vmRule.addRuleItem(vmRuleItem);
            }

            vmRules.add(vmRule);
        }

        return vmRules;
    }
}


