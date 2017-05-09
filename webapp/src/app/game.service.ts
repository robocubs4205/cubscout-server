import { Injectable } from '@angular/core';
import {Http, Response, RequestOptions, Headers} from "@angular/http";
import {environment} from "../environments/environment";
import {Observable} from "rxjs";
import {Game} from "./game";
import {ApiRoot} from "./api-root";
import {Link} from "./link";

@Injectable()
export class GameService {

  getAllGames(root:ApiRoot):Observable<Game[]>{
    let link:Link = root.links.find(link=>link.rel=="games");
    if(link==null) return Observable.throw("ApiRoot is missing a link with rel \"games\"");
    return this.http.get(link.href,new RequestOptions({
      headers: new Headers([
        {'accept': 'application/vnd.robocubs-v1+json'}
      ])
    }))
      .map(res=>res.json().data).catch(error=>{
      let errMsg: string;
      if (error instanceof Response) {
        const body = error.json() || '';
        const err = body.error || JSON.stringify(body);
        errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
        errMsg+=JSON.stringify(error);
      } else {
        errMsg = error.message ? error.message : error.toString();
      }
      console.error("failed to retrieve games");
      console.error(errMsg);
      return Observable.throw(errMsg);
    });
  }

  constructor(private http: Http) { }

}
