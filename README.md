# json-sql
Json-sql is a command line client which accepts sql "like" commands to query json source (supports REST GET only at the moment). 

The json-sql is spring based command line tool, which accepts the rest get endpoint as alias name & performs rest calls using sql query like commands.

To start the command line download the java executable from the below link,

[Download V0.1-beta](https://github.com/saranrajj/json-sql/blob/master/dist/V-0.1.zip?raw=true)
and run it as,

    > java -jar json-sql.jar

# Supported Commands:

# 1. config add:
   command allows to configure the endpoint as alias, this will be used later to query using sql like commands (similar to view in database).
   
   >config add --alias currency --source https://api.exchangeratesapi.io/latest
  
   In the above command, the config  add command will take 2 parameters 
      --alias (name for the endpoint you like to query) 
      --source (endpoint url)
      
   Note: Adding the same endpoint will overwrite the existing config.
 
 # 2. config get
 command allows to fetch the config added already, 
   
   >config get --alias currency
  
 In the above command, the config get takes just one parameter,
     --alias (name of the alias name you want to fetch)

 # 3. select Commands:
 Well this is used to fetch the data from the alias endpoint using get call.
  
  - Just fetch with out formatting or conditions (basically i want all the data)
     
    > select * from currency
      
    Output:
        Response headers:
        Date: [Thu, 28 Nov 2019 22:30:46 GMT]
        Response code:200
        {"rates":{"CAD":1.4625,"HKD":8.6136,"ISK":135.0,"PHP":55.9,"DKK":7.4715,"HUF":336.25,"CZK":25.574,"AUD":1.6253,"RON":4.785,"SEK":10.5463,"IDR":15517.93,"INR":78.8095,"BRL":4.6793,"RUB":70.5328,"HRK":7.437,"JPY":120.5,"THB":33.263,"CHF":1.0991,"SGD":1.5032,"PLN":4.3212,"BGN":1.9558,"TRY":6.3477,"CNY":7.7416,"NOK":10.113,"NZD":1.7136,"ZAR":16.2298,"USD":1.1005,"MXN":21.5787,"ILS":3.817,"GBP":0.8518,"KRW":1298.0,"MYR":4.5929},"base":"EUR","date":"2019-11-28"}
   
   Note: "select *" do not care about the output format it just makes the rest call and do not support the conditions.
   
   - Query with column names, (The columns should be with in double quotes or comma-seperated without space, courtesy no support for infinite arity in spring shell yet :( )
   
    > select "rates.CAD,rates.DKK" from currency
    
    Output:
    Response headers:
    Date: [Thu, 28 Nov 2019 22:41:39 GMT]
           rates.CAD |           rates.DKK |
              1.4625 |              7.4715 |
   
   Yay finally some table format like sql!!!
   
   Let write some query with condition, (only and is supported for now, will add more operator support soon, condition as well needs to be enclosed with double quotes).
   
   > select "rates.CAD,rates.DKK" from currency where "rates.CAD=1.4625 and rates.DKK=7.4715"
      Response headers:
      Date: [Thu, 28 Nov 2019 22:48:18 GMT]
                 rates.CAD |           rates.DKK |
                    1.4625 |              7.4715 |

See more example in the article below. 
https://medium.com/@saranraj.j76/sneak-peek-on-the-json-sql-like-command-client-186e3a1ae1ff

# Features in plan for future:
  1.  More conditional support (or condition, >,<, != operations in conditions)
  2.  Support for auth headers natively
  3.  Join queries to fetch & join from multiple sources.

