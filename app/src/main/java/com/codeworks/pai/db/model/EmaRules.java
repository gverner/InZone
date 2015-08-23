package com.codeworks.pai.db.model;

import android.content.res.Resources;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.R;
import com.codeworks.pai.processor.Notice;
import com.codeworks.pai.study.Period;

public class EmaRules extends RulesBase {

    protected static double ZONE_INNER = 0.5d;
    protected static double ZONE_OUTER = 2d;

    public EmaRules(Study study) {
        this.study = study;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.codeworks.pai.db.model.Rules#calcUpperSellZoneTop(com.codeworks.pai
     * .study.Period)
     */
    @Override
    public double calcUpperSellZoneTop(Period period) {
        return calcUpperSellZoneBottom(period) + pierceOffset(period);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.codeworks.pai.db.model.Rules#calcUpperSellZoneBottom(com.codeworks
     * .pai.study.Period)
     */
    @Override
    public double calcUpperSellZoneBottom(Period period) {
        if (Period.Week.equals(period)) {
            return study.getEmaWeek() + (study.getEmaStddevWeek() * ZONE_OUTER);
        } else {
            return study.getEmaMonth() + (study.getEmaStddevMonth() * ZONE_OUTER);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.codeworks.pai.db.model.Rules#calcUpperBuyZoneTop(com.codeworks.pai
     * .study.Period)
     */
    @Override
    public double calcUpperBuyZoneTop(Period period) {
        if (Period.Week.equals(period)) {
            return study.getEmaWeek() + (study.getEmaStddevWeek() * ZONE_INNER);
        } else {
            return study.getEmaMonth() + (study.getEmaStddevMonth() * ZONE_INNER);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.codeworks.pai.db.model.Rules#calcUpperBuyZoneBottom(com.codeworks
     * .pai.study.Period)
     */
    @Override
    public double calcUpperBuyZoneBottom(Period period) {
        if (Period.Week.equals(period)) {
            return study.getEmaWeek();
        } else {
            return study.getEmaMonth();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.codeworks.pai.db.model.Rules#calcLowerSellZoneTop(com.codeworks.pai
     * .study.Period)
     */
    @Override
    public double calcLowerSellZoneTop(Period period) {
        if (Period.Week.equals(period)) {
            return study.getEmaWeek();
        } else {
            return study.getEmaMonth();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.codeworks.pai.db.model.Rules#calcLowerSellZoneBottom(com.codeworks
     * .pai.study.Period)
     */
    @Override
    public double calcLowerSellZoneBottom(Period period) {
        if (Period.Week.equals(period)) {
            return study.getEmaWeek() - (study.getEmaStddevWeek() * ZONE_INNER);
        } else {
            return study.getEmaMonth() - (study.getEmaStddevMonth() * ZONE_INNER);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.codeworks.pai.db.model.Rules#calcLowerBuyZoneTop(com.codeworks.pai
     * .study.Period)
     */
    @Override
    public double calcLowerBuyZoneTop(Period period) {
        if (Period.Week.equals(period)) {
            return study.getEmaWeek() - (study.getEmaStddevWeek() * ZONE_OUTER);
        } else {
            return study.getEmaMonth() - (study.getEmaStddevMonth() * ZONE_OUTER);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.codeworks.pai.db.model.Rules#calcLowerBuyZoneBottom(com.codeworks
     * .pai.study.Period)
     */
    @Override
    public double calcLowerBuyZoneBottom(Period period) {
        return calcLowerBuyZoneTop(period) - pierceOffset(period);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.codeworks.pai.db.model.Rules#calcBuyZoneBottom()
     */
    @Override
    public double calcBuyZoneBottom() {
        if (study.getEmaWeek() == Double.NaN || study.getEmaStddevWeek() == Double.NaN) {
            return 0;
        }
        if (isUpTrendWeekly()) {
            return study.getEmaWeek();
        } else {
            if (isWeeklyLowerBuyZoneCompressedByMonthly()) {
                return calcLowerBuyZoneBottom(Period.Month);
            } else {
                return calcLowerBuyZoneBottom(Period.Week);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.codeworks.pai.db.model.Rules#calcBuyZoneTop()
     */
    @Override
    public double calcBuyZoneTop() {
        if (study.getEmaWeek() == Double.NaN || study.getEmaStddevWeek() == Double.NaN) {
            return 0;
        }
        if (isUpTrendWeekly()) {
            return study.getEmaWeek() + (study.getEmaStddevWeek() * ZONE_INNER);
        } else {
            if (isWeeklyLowerBuyZoneCompressedByMonthly()) {
                return calcLowerBuyZoneTop(Period.Month);
            } else {
                return calcLowerBuyZoneTop(Period.Week);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.codeworks.pai.db.model.Rules#calcSellZoneBottom()
     */
    @Override
    public double calcSellZoneBottom() {
        if (study.getEmaWeek() == Double.NaN || study.getEmaStddevWeek() == Double.NaN) {
            return 0;
        }
        if (isUpTrendWeekly()) {
            if (isWeeklyUpperSellZoneExpandedByMonthly()) {
                return calcUpperSellZoneBottom(Period.Month);
            } else {
                return study.getEmaWeek() + (study.getEmaStddevWeek() * ZONE_OUTER);
            }
        } else {
            return study.getEmaWeek() - (study.getEmaStddevWeek() * ZONE_INNER);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.codeworks.pai.db.model.Rules#calcSellZoneTop()
     */
    @Override
    public double calcSellZoneTop() {
        if (study.getEmaWeek() == Double.NaN || study.getEmaStddevWeek() == Double.NaN) {
            return 0;
        }
        if (isUpTrendWeekly()) {
            return study.getEmaWeek() + (study.getEmaStddevWeek() * ZONE_OUTER) + pierceOffset(Period.Week);
        } else {
            return study.getEmaWeek();
        }
    }

    @Override
    public double AOBPut() {
        double buyZoneTop = calcBuyZoneTop();
        if (isDownTrendMonthly() && isDownTrendWeekly() && (buyZoneTop > study.getPrice())) {
            buyZoneTop = study.getPrice();
        }
        double AOBBUY = PaiUtils.round(Math.floor(buyZoneTop), 0);
        return AOBBUY;
    }

    @Override
    public double AOACall() {
        double sellZoneBottom = calcSellZoneBottom();
        double AOBSELL = PaiUtils.round(Math.ceil(sellZoneBottom), 0);
        return AOBSELL;
    }

    @Override
    public boolean isWeeklyUpperSellZoneExpandedByMonthly() {
        if (isUpTrendWeekly() && calcUpperSellZoneBottom(Period.Month) < calcUpperSellZoneBottom(Period.Week) && !study.hasInsufficientHistory()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isWeeklyLowerBuyZoneCompressedByMonthly() {

        if (isDownTrendWeekly() && isDownTrendMonthly()) { // && calcLowerBuyZoneTop(Period.Month) < calcLowerBuyZoneTop(Period.Week)) {
            return true;
        } else {
            return false;
        }
    }

    double pierceOffset(Period period) {
        return (study.getPrice() / 100d) * (Period.Week.equals(period) ? 2d : 5d);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.codeworks.pai.db.model.Rules#isPriceInBuyZone()
     */
    @Override
    public boolean isPriceInBuyZone() {
        return (study.getPrice() >= calcBuyZoneBottom() && study.getPrice() <= calcBuyZoneTop());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.codeworks.pai.db.model.Rules#isPriceInSellZone()
     */
    @Override
    public boolean isPriceInSellZone() {
        return (study.getPrice() >= calcSellZoneBottom());// && price <=
        // calcSellZoneTop());
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * com.codeworks.pai.db.model.Rules#isUpTrend(com.codeworks.pai.study.Period
     * )
     */
    @Override
    public boolean isUpTrend(Period period) {
        if (Period.Month.equals(period)) {
            return study.getEmaMonth() <= study.getPrice();
        } else {
            return study.getEmaLastWeek() <= study.getPriceLastWeek();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.codeworks.pai.db.model.Rules#isPossibleTrendTerminationWeekly()
     */
    @Override
    public boolean isPossibleTrendTerminationWeekly() {
        return isPossibleDowntrendTermination(Period.Week) || isPossibleUptrendTermination(Period.Week);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Symbol=");
        sb.append(study.getSymbol());
        sb.append(" ema=");
        sb.append(Study.format(study.getEmaWeek()));
        sb.append(" buy zone bottom=");
        sb.append(Study.format(this.calcBuyZoneBottom()));
        sb.append(" top=");
        sb.append(Study.format(this.calcBuyZoneTop()));
        sb.append(" sell zone bottom=");
        sb.append(Study.format(this.calcSellZoneBottom()));
        sb.append(" top=");
        sb.append(Study.format(this.calcSellZoneTop()));
        sb.append(" WUT=" + isUpTrendWeekly());
        sb.append(" MUT=" + isUpTrendMonthly());
        sb.append(" PLW=" + Study.format(study.getPriceLastWeek()));
        sb.append(" maLM=" + Study.format(study.getEmaLastMonth()));
        sb.append(" PLM=" + Study.format(study.getPriceLastMonth()));
        return sb.toString();
    }


    @Override
    public StringBuilder getAdditionalAlerts(Resources res) {
        StringBuilder alert = super.getAdditionalAlerts(res);

        if (hasTradedBelowMAToday()) {
            alert.append(res.getString(R.string.alert_has_traded_below_ma));
        }
        if (isWeeklyUpperSellZoneExpandedByMonthly()) {
            if (alert.length() > 0) {
                alert.append("\n");
            }
            alert.append(res.getString(R.string.alert_sell_zone_expanded_by_monthly));
        }
        return alert;
    }

    @Override
    public void updateNotice() {
        if (isPossibleDowntrendTermination(Period.Week)) {
            study.setNotice(Notice.POSSIBLE_WEEKLY_DOWNTREND_TERMINATION);
        } else if (isPossibleUptrendTermination(Period.Week)) {
            study.setNotice(Notice.POSSIBLE_WEEKLY_UPTREND_TEMINATION);
        } else if (isPriceInBuyZone()) {
            study.setNotice(Notice.IN_BUY_ZONE);
        } else if (isPriceInSellZone()) {
            study.setNotice(Notice.IN_SELL_ZONE);
        } else {
            study.setNotice(Notice.NONE);
        }
    }

    @Override
    public String inCash() {
        String rule = "";
        if (isUpTrendWeekly()) {
            double AOBBUY = AOBPut();
            if (isPossibleUptrendTermination(Period.Week)) {
                rule = "Place Stop Buy Order at moving average + 1/4 Average True Range(ATR)";
            } else if (isPriceInBuyZone()) {
                rule = "C: Sell Puts in the Buy Zone AOB " + Double.toString(AOBBUY) + "p\nA: Buy Stock";
            } else if (isPriceInSellZone()) {
                rule = "Sell Puts in the Buy Zone AOB " + Double.toString(AOBBUY) + "p";
            } else {
                rule = "Sell puts in the Buy Zone AOB " + Double.toString(AOBBUY) + "p";
            }
        } else { // Weekly DownTrend
            if (isUpTrendMonthly()) {
                double AOBBUY = AOBPut();
                if (isPriceInBuyZone()) {
                    rule = "C: Sell Puts in the Buy Zone AOB " + Double.toString(AOBBUY) + "p\nA: Buy Stock";
                } else if (isPriceInSellZone()) {
                    rule = "Sell Puts in the Buy Zone AOB " + Double.toString(AOBBUY) + "p";
                } else {
                    rule = "Sell puts in the Buy Zone AOB " + Double.toString(AOBBUY) + "p";
                }
            } else { // Monthly DownTrend
                if (isPossibleDowntrendTermination(Period.Week)) {
                    rule = "Wait for Weekly Close above moving average";
                } else {
                    rule = "C: Sell Puts at (MPR) Monthly Probable Range or (PDL) Proximal Demand Level";
                }

            }
        }
        return rule;
    }

    @Override
    public String inCashAndPut() {
        String rule = "";
        if (isUpTrendWeekly()) {
            if (isPossibleUptrendTermination(Period.Week)) {
                rule = "Buy back Put and Place Stock Stop Buy Order at moving average + 1/4 Average True Range(ATR)";
            } else if (isPriceInBuyZone()) {
                rule = "C: Going For the Ride\nA: Buy Back Put and Buy Stock";
            } else if (isPriceInSellZone()) {
                rule = "Going for the Ride";
            } else {
                rule = "Going for the Ride";
            }
        } else { // Weekly DownTrend
            if (isUpTrendMonthly()) {
                if (isPriceInBuyZone()) {
                    rule = "C: Going for the Ride\nA: Buy Stock";
                } else if (isPriceInSellZone()) {
                    rule = "Going for the Ride";
                } else {
                    rule = "Going for the Ride";
                }
            } else { // Monthly DownTrend
                if (isPossibleDowntrendTermination(Period.Week)) {
                    rule = "Wait for Weekly Close above moving average";
                } else {
                    rule = "C: Roll Puts, Buy back Puts and Sell Puts at (MPR/PDL)";
                }
            }
        }
        return rule;
    }

    @Override
    public String inStock() {
        String rule = "";
        if (isUpTrendWeekly()) {
            double AOASELL = AOACall();
            if (isPossibleUptrendTermination(Period.Week)) {
                rule = "Sell Stock and Place Stop Buy Order at moving average + 1/4 Average True Range(ATR)";
            } else if (isPriceInBuyZone()) {
                rule = "Be a willing Seller by Selling Calls in Sell Zone AOA " + Double.toString(AOASELL) + "c";
            } else if (isPriceInSellZone()) {
                double PRICE = PaiUtils.round(Math.ceil(study.getPrice()), 0);
                rule = "C: Sell Stock\nA: Sell Calls AOA" + PRICE + "c and place a stop loss to Buy Back Call and Sell Stock at " + PaiUtils.round(calcSellZoneBottom());
            } else {
                rule = "Be a willing Seller by Selling Calls in Sell Zone AOA " + Double.toString(AOASELL) + "c";
            }
        } else { // Weekly DownTrend
            if (isUpTrendMonthly()) {
                if (isPriceInBuyZone()) {
                    rule = "Going for the Ride";
                } else if (isPriceInSellZone()) {
                    rule = "C: Sell Stock\nA: Place Stop Loss order at bottom of lower Sell Zone " + PaiUtils.round(calcSellZoneBottom());
                } else {
                    rule = "Going for the Ride";
                }
            } else { // Monthly DownTrend
                if (isPossibleDowntrendTermination(Period.Week)) {
                    rule = "Sell Stock and Wait for Weekly Close above moving average";
                } else if (isPriceInSellZone()) {
                    rule = "Sell Stock and Sell Puts at (MPR/PDL)";
                } else if (isPriceInBuyZone()) {
                    rule = "Sell Stock and Sell Puts at (MPR/PDL)";
                } else {
                    rule = "Sell Stock and Sell Puts at (MPR/PDL)";
                }
            }
        }
        return rule;
    }

    @Override
    public String inStockAndCall() {
        String rule = "";
        if (isUpTrendWeekly()) {
            if (isPossibleUptrendTermination(Period.Week)) {
                rule = "Buy Back Calls, Sell Stock and Place Stop Buy Order at moving average + 1/4 Average True Range(ATR)";
            } else if (isPriceInBuyZone()) {
                rule = "Going for the Ride";
            } else if (isPriceInSellZone()) {
                rule = "C: Buy Back Calls and Sell Stock\nA: Place stop lost order to Buy Back Calls and Sell Stock at bottom of upper Sell Zone "
                        + PaiUtils.round(calcSellZoneBottom());
            } else {
                rule = "Going for the Ride";
            }
        } else { // Weekly DownTrend
            if (isUpTrendMonthly()) {
                if (isPriceInBuyZone()) {
                    rule = "Going for the Ride";
                } else if (isPriceInSellZone()) {
                    rule = "C: Buy Back Calls and Sell Stock\nA: Place Stop Loss order at bottom of lower Sell Zone at " + PaiUtils.round(calcSellZoneBottom())
                            + " to Buy Back Calls and Sell Stock";
                } else {
                    rule = "Going for the Ride";
                }
            } else { // Monthly DownTrend
                if (isPossibleDowntrendTermination(Period.Week)) {
                    rule = "Buy Back Calls, Sell Stock and Wait for Weekly Close above moving average";
                } else if (isPriceInSellZone()) {
                    rule = "Buy Back Calls, Sell Stock and Sell Puts at (MPR/PDL)";
                } else if (isPriceInBuyZone()) {
                    rule = "Buy Back Calls, Sell Stock and Sell Puts at (MPR/PDL)";
                } else {
                    rule = "Buy Back Calls, Sell Stock and Sell Puts at (MPR/PDL)";
                }

            }
        }
        return rule;
    }

    @Override
    public MaType getMaType() {
        return MaType.E;
    }

}
