import { Injectable } from '@angular/core';
import {Http, Response, Headers, RequestOptions} from "@angular/http";
import {ApiRoot} from "./api-root";
import {Observable} from "rxjs";
import {environment} from "../environments/environment";

@Injectable()
export class ApiRootService {

  getRoot():Observable<ApiRoot>{
    return this.http.get(environment.apiBaseUrl,new RequestOptions({
      headers: new Headers([
        {'accept': 'application/vnd.robocubs-v1+json'}
      ])
    })).map(res=>res.json()).catch(error=>{
      let errMsg: string;
      if (error instanceof Response) {
        const body = error.json() || '';
        const err = body.error || JSON.stringify(body);
        errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
        errMsg+=JSON.stringify(error);
      } else {
        errMsg = error.message ? error.message : error.toString();
      }
      console.error("failed to retrieve api root");
      console.error(errMsg);
      return Observable.throw(errMsg);
    });
  }

  constructor(private http:Http) { }

}
