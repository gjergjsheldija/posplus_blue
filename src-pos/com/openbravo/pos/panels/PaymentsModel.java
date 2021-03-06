//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2008 Openbravo, S.L.
//    http://sourceforge.net/projects/openbravopos
//
//    This program is free software; you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation; either version 2 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package com.openbravo.pos.panels;

import java.util.*;
import javax.swing.table.AbstractTableModel;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;

/**
 *
 * @author adrianromero
 */
public class PaymentsModel {

    private String m_sHost;
    private int m_iSeq;
    private Date m_dDateStart;
    private Date m_dDateEnd;

    private Integer m_iPayments;
    private Double m_dPaymentsTotal;
    private java.util.List<PaymentsLine> m_lpayments;

    private final static String[] PAYMENTHEADERS = {"Label.Payment", "label.totalcash"};

    private Integer m_iSales;
    private Double m_dSalesBase;
    private Double m_dSalesTaxes;
    private java.util.List<SalesLine> m_lsales;

    private String m_sUserName;

    private final static String[] SALEHEADERS = {"label.taxcash", "label.totalcash"};

    private PaymentsModel() {
    }

    public static PaymentsModel emptyInstance() {

        PaymentsModel p = new PaymentsModel();

        p.m_iPayments = new Integer(0);
        p.m_dPaymentsTotal = new Double(0.0);
        p.m_lpayments = new ArrayList<PaymentsLine>();

        p.m_iSales = null;
        p.m_dSalesBase = null;
        p.m_dSalesTaxes = null;
        p.m_lsales = new ArrayList<SalesLine>();
        p.m_sUserName = null;

        return p;
    }

    public static PaymentsModel loadInstance(AppView app) throws BasicException {

        PaymentsModel p = new PaymentsModel();

        // Propiedades globales
        p.m_sHost = app.getProperties().getHost();
        p.m_iSeq = app.getActiveCashSequence();
        p.m_dDateStart = app.getActiveCashDateStart();
        p.m_dDateEnd = null;
        p.m_sUserName = app.getAppUserView().getUser().getName();

        //Pagos
        if(app.getAppUserView().getUser().hasPermission("sales.CloseCash") != true) {
            PreparedSentence ps = new PreparedSentence(app.getSession()
                    , "SELECT COUNT(*), SUM(PAYMENTS.TOTAL) " +
                        "FROM PAYMENTS, RECEIPTS,TICKETS " +
                        "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID " +
                        "AND TICKETS.ID =  RECEIPTS.ID " +
                        "AND RECEIPTS.MONEY = ? " +
                        "AND TICKETS.PERSON = ? "
                    , new SerializerWriteBasic(new Datas[]{ Datas.STRING, Datas.STRING})
                    , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}));

            Object[] valtickets = (Object []) ps.find(new Object[]{
                    app.getActiveCashIndex(),
                    app.getAppUserView().getUser().getUserInfo().getId()
            });

            if (valtickets == null) {
                p.m_iPayments = new Integer(0);
                p.m_dPaymentsTotal = new Double(0.0);
            } else {
                p.m_iPayments = (Integer) valtickets[0];
                p.m_dPaymentsTotal = (Double) valtickets[1];
            }
        } else {
            PreparedSentence ps = new PreparedSentence(app.getSession()
                    , "SELECT COUNT(*), SUM(PAYMENTS.TOTAL) " +
                        "FROM PAYMENTS, RECEIPTS,TICKETS " +
                        "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID " +
                        "AND TICKETS.ID =  RECEIPTS.ID " +
                        "AND RECEIPTS.MONEY = ? "
                    , new SerializerWriteBasic(new Datas[]{ Datas.STRING})
                    , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}));

            Object[] valtickets = (Object []) ps.find(new Object[]{
                    app.getActiveCashIndex()
            });

            if (valtickets == null) {
                p.m_iPayments = new Integer(0);
                p.m_dPaymentsTotal = new Double(0.0);
            } else {
                p.m_iPayments = (Integer) valtickets[0];
                p.m_dPaymentsTotal = (Double) valtickets[1];
            }
        }

        if(app.getAppUserView().getUser().hasPermission("sales.CloseCash") != true) {
            List l = new StaticSentence(app.getSession()
                , "SELECT PAYMENTS.PAYMENT, SUM(PAYMENTS.TOTAL) " +
                  "FROM PAYMENTS, RECEIPTS,TICKETS  " +
                  "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID AND RECEIPTS.MONEY = ? " +
                  "AND TICKETS.ID =  RECEIPTS.ID " +
                  "AND TICKETS.PERSON = ? " +
                  "GROUP BY PAYMENTS.PAYMENT"
                , new SerializerWriteBasic(new Datas[]{ Datas.STRING, Datas.STRING})
                , new SerializerReadClass(PaymentsModel.PaymentsLine.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
                .list(new Object[] {app.getActiveCashIndex(),app.getAppUserView().getUser().getUserInfo().getId() } );

            if (l == null) {
                p.m_lpayments = new ArrayList();
            } else {
                p.m_lpayments = l;
            }
        } else {
            List l = new StaticSentence(app.getSession()
                , "SELECT PAYMENTS.PAYMENT, SUM(PAYMENTS.TOTAL) " +
                  "FROM PAYMENTS, RECEIPTS,TICKETS  " +
                  "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID AND RECEIPTS.MONEY = ? " +
                  "AND TICKETS.ID =  RECEIPTS.ID " +
                  "GROUP BY PAYMENTS.PAYMENT"
                , new SerializerWriteBasic(new Datas[]{ Datas.STRING })
                , new SerializerReadClass(PaymentsModel.PaymentsLine.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
                .list(new Object[] {app.getActiveCashIndex() } );

            if (l == null) {
                p.m_lpayments = new ArrayList();
            } else {
                p.m_lpayments = l;
            }
        }


        // Sales
        if(app.getAppUserView().getUser().hasPermission("sales.CloseCash") != true) {
            Object[] recsales = (Object []) new StaticSentence(app.getSession(),
                "SELECT COUNT(DISTINCT RECEIPTS.ID), SUM(TICKETLINES.UNITS * TICKETLINES.PRICE) " +
                "FROM RECEIPTS, TICKETLINES, TICKETS WHERE RECEIPTS.ID = TICKETLINES.TICKET AND RECEIPTS.MONEY = ?" +
                "AND TICKETS.ID =  RECEIPTS.ID " +
                "AND TICKETS.PERSON = ? ",
                new SerializerWriteBasic(new Datas[]{ Datas.STRING, Datas.STRING}),
                new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
                .find(new Object[] {app.getActiveCashIndex(),app.getAppUserView().getUser().getUserInfo().getId() } );
            if (recsales == null) {
                p.m_iSales = null;
                p.m_dSalesBase = null;
            } else {
                p.m_iSales = (Integer) recsales[0];
                p.m_dSalesBase = (Double) recsales[1];
            }
        } else {
            Object[] recsales = (Object []) new StaticSentence(app.getSession(),
                "SELECT COUNT(DISTINCT RECEIPTS.ID), SUM(TICKETLINES.UNITS * TICKETLINES.PRICE) " +
                "FROM RECEIPTS, TICKETLINES, TICKETS WHERE RECEIPTS.ID = TICKETLINES.TICKET AND RECEIPTS.MONEY = ?" +
                "AND TICKETS.ID =  RECEIPTS.ID ",
                new SerializerWriteBasic(new Datas[]{ Datas.STRING }),
                new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
                .find(new Object[] {app.getActiveCashIndex()  } );
            if (recsales == null) {
                p.m_iSales = null;
                p.m_dSalesBase = null;
            } else {
                p.m_iSales = (Integer) recsales[0];
                p.m_dSalesBase = (Double) recsales[1];
            }
        }


        // Taxes
        Object[] rectaxes = (Object []) new StaticSentence(app.getSession(),
            "SELECT SUM(TAXLINES.AMOUNT) " +
            "FROM RECEIPTS, TAXLINES, TICKETS WHERE RECEIPTS.ID = TAXLINES.RECEIPT AND RECEIPTS.MONEY = ?" +
            "AND TICKETS.ID =  RECEIPTS.ID " 
            , new SerializerWriteBasic(new Datas[]{ Datas.STRING })
            , new SerializerReadBasic(new Datas[] {Datas.DOUBLE}))
             .find(new Object[] {app.getActiveCashIndex()} );
        if (rectaxes == null) {
            p.m_dSalesTaxes = null;
        } else {
            p.m_dSalesTaxes = (Double) rectaxes[0];
        }

        if(app.getAppUserView().getUser().hasPermission("sales.CloseCash") != true) {
            List<SalesLine> asales = new StaticSentence(app.getSession(),
                    "SELECT TAXCATEGORIES.NAME, SUM(TAXLINES.AMOUNT) " +
                    "FROM RECEIPTS, TAXLINES, TICKETS, TAXES, TAXCATEGORIES WHERE RECEIPTS.ID = TAXLINES.RECEIPT AND TAXLINES.TAXID = TAXES.ID AND TAXES.CATEGORY = TAXCATEGORIES.ID " +
                    "AND RECEIPTS.MONEY = ?" +
                    "AND TICKETS.ID =  RECEIPTS.ID " +
                    "AND TICKETS.PERSON = ? " +
                    "GROUP BY TAXCATEGORIES.NAME"
                    , new SerializerWriteBasic(new Datas[]{ Datas.STRING, Datas.STRING})
                    , new SerializerReadClass(PaymentsModel.SalesLine.class))
                    .list(new Object[] {app.getActiveCashIndex(),app.getAppUserView().getUser().getUserInfo().getId() } );
            if (asales == null) {
                p.m_lsales = new ArrayList<SalesLine>();
            } else {
                p.m_lsales = asales;
            }
        } else {
            List<SalesLine> asales = new StaticSentence(app.getSession(),
                    "SELECT TAXCATEGORIES.NAME, SUM(TAXLINES.AMOUNT) " +
                    "FROM RECEIPTS, TAXLINES, TICKETS, TAXES, TAXCATEGORIES WHERE RECEIPTS.ID = TAXLINES.RECEIPT AND TAXLINES.TAXID = TAXES.ID AND TAXES.CATEGORY = TAXCATEGORIES.ID " +
                    "AND RECEIPTS.MONEY = ?" +
                    "AND TICKETS.ID =  RECEIPTS.ID " +
                    "GROUP BY TAXCATEGORIES.NAME"
                    , new SerializerWriteBasic(new Datas[]{ Datas.STRING })
                    , new SerializerReadClass(PaymentsModel.SalesLine.class))
                    .list(new Object[] {app.getActiveCashIndex() } );
            if (asales == null) {
                p.m_lsales = new ArrayList<SalesLine>();
            } else {
                p.m_lsales = asales;
            }
        }

        return p;
    }

    public int getPayments() {
        return m_iPayments.intValue();
    }
    public double getTotal() {
        return m_dPaymentsTotal.doubleValue();
    }
    public String getHost() {
        return m_sHost;
    }
    public int getSequence() {
        return m_iSeq;
    }
    public Date getDateStart() {
        return m_dDateStart;
    }
    public void setDateEnd(Date dValue) {
        m_dDateEnd = dValue;
    }
    
    public Date getDateEnd() {
        return m_dDateEnd;
    }

    public String printUserName() {
        return m_sUserName;
    }

    public String printHost() {
        return m_sHost;
    }
    public String printSequence() {
        return Formats.INT.formatValue(m_iSeq);
    }
    public String printDateStart() {
        return Formats.TIMESTAMP.formatValue(m_dDateStart);
    }
    public String printDateEnd() {
        return Formats.TIMESTAMP.formatValue(m_dDateEnd);
    }

    public String printPayments() {
        return Formats.INT.formatValue(m_iPayments);
    }

    public String printPaymentsTotal() {
        return Formats.CURRENCY.formatValue(m_dPaymentsTotal);
    }

    public List<PaymentsLine> getPaymentLines() {
        return m_lpayments;
    }

    public int getSales() {
        return m_iSales == null ? 0 : m_iSales.intValue();
    }
    public String printSales() {
        return Formats.INT.formatValue(m_iSales);
    }
    public String printSalesBase() {
        return Formats.CURRENCY.formatValue(m_dSalesBase);
    }
    public String printSalesTaxes() {
        return Formats.CURRENCY.formatValue(m_dSalesTaxes);
    }
    public String printSalesTotal() {
        return Formats.CURRENCY.formatValue((m_dSalesBase == null || m_dSalesTaxes == null)
                ? null
                : m_dSalesBase + m_dSalesTaxes);
    }
    public List<SalesLine> getSaleLines() {
        return m_lsales;
    }

    public AbstractTableModel getPaymentsModel() {
        return new AbstractTableModel() {
            public String getColumnName(int column) {
                return AppLocal.getIntString(PAYMENTHEADERS[column]);
            }
            public int getRowCount() {
                return m_lpayments.size();
            }
            public int getColumnCount() {
                return PAYMENTHEADERS.length;
            }
            public Object getValueAt(int row, int column) {
                PaymentsLine l = m_lpayments.get(row);
                switch (column) {
                case 0: return l.getType();
                case 1: return l.getValue();
                default: return null;
                }
            }
        };
    }

    public static class SalesLine implements SerializableRead {

        private String m_SalesTaxName;
        private Double m_SalesTaxes;

        public void readValues(DataRead dr) throws BasicException {
            m_SalesTaxName = dr.getString(1);
            m_SalesTaxes = dr.getDouble(2);
        }
        public String printTaxName() {
            return m_SalesTaxName;
        }
        public String printTaxes() {
            return Formats.CURRENCY.formatValue(m_SalesTaxes);
        }
        public String getTaxName() {
            return m_SalesTaxName;
        }
        public Double getTaxes() {
            return m_SalesTaxes;
        }
    }

    public AbstractTableModel getSalesModel() {
        return new AbstractTableModel() {
            public String getColumnName(int column) {
                return AppLocal.getIntString(SALEHEADERS[column]);
            }
            public int getRowCount() {
                return m_lsales.size();
            }
            public int getColumnCount() {
                return SALEHEADERS.length;
            }
            public Object getValueAt(int row, int column) {
                SalesLine l = m_lsales.get(row);
                switch (column) {
                case 0: return l.getTaxName();
                case 1: return l.getTaxes();
                default: return null;
                }
            }
        };
    }

    public static class PaymentsLine implements SerializableRead {

        private String m_PaymentType;
        private Double m_PaymentValue;

        public void readValues(DataRead dr) throws BasicException {
            m_PaymentType = dr.getString(1);
            m_PaymentValue = dr.getDouble(2);
        }

        public String printType() {
            return AppLocal.getIntString("transpayment." + m_PaymentType);
        }
        public String getType() {
            return m_PaymentType;
        }
        public String printValue() {
            return Formats.CURRENCY.formatValue(m_PaymentValue);
        }
        public Double getValue() {
            return m_PaymentValue;
        }
    }
}    