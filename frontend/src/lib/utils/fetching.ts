import { addToastByRes, addToast } from "$utils/addToToastStore";
import { jwt } from "$stores/jwtStore";
import { get } from "svelte/store";
import { handleLogout } from "$utils/handleLogout";

export interface Params {
  name: string;
  value: string | boolean;
}

/**
 * a general fetching function for all fetch requests
 * @param url the url to fetch
 * @param method the method to use
 * @param data the data to send OPTIONAL
 * @param json if the data is json O
 * @param params the params to add to the url OPTIONAL
 * @returns the response
 * @example fetching('http://localhost:3000/api/v1/users', 'GET', null, false, [{limit: 10}, {offset: 0}])
 */
export async function fetching(url: string, method: string, params?: Params[], data?, json?:boolean): json{
  let requestOptions;
  let myHeaders = new Headers(); 
  myHeaders.append("Authorization", JSON.stringify(get(jwt)));
  
  if(params){
    for(let i=0; i<params.length; i++){
      if(i === 0){
        url = url + '?' + params[i].name + '=' + params[i].value;
      }else{
        url = url + '&' + params[i].name + '=' + params[i].value;
      }
    }
  }
  
  if(method === 'GET'){
    requestOptions = {
      method: method,
      headers: myHeaders,
    };
  }
  else if(json && (method === 'POST' || method === 'PUT' || method === 'DELETE')){
    myHeaders.append("Content-Type", "application/json");
    requestOptions = {
      method: method,
      headers: myHeaders,
      body: JSON.stringify(data),
    };
  }
  else if(!json && (method === 'POST' || method === 'PUT' || method === 'DELETE')){
    requestOptions = {
      method: method,
      headers: myHeaders,
      body: data,
    };
  }

  let res = await fetch(url, requestOptions);
  res = await res.json();
  if(res.type === "TokenExpired"){
    handleLogout(true);
    addToast("Your session has expired, please log in again");
    return;
  }
  return res;
}

