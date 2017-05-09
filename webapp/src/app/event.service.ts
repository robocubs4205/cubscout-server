import { Injectable } from '@angular/core';
import {Game} from "./game";
import {Observable} from "rxjs";
import {Http, Response, RequestOptions, Headers} from "@angular/http";
import {environment} from "../environments/environment";
import {Link} from "./link";
import {Event} from "./event"

@Injectable()
export class EventService {

  getEventsByGame(game:Game):Observable<Event[]>{
    let link:Link = game.links.find(link=>link.rel=="events");
    if(link==null) return Observable.throw("game does not have link with rel \"events\"");
    return this.http.get(link.href,new RequestOptions({
      headers: new Headers([
        {'accept': 'application/vnd.robocubs-v1+json'}
      ])
    })).map(res=>res.json().data).catch(error=>{
      let errMsg: string;
      if (error instanceof Response) {
        const body = error.json() || '';
        const err = body.error || JSON.stringify(body);
        errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
        errMsg+=JSON.stringify(error);
      } else {
        errMsg = error.message ? error.message : error.toString();
      }
      console.error("failed to retrieve scorecard");
      console.error(errMsg);
      return Observable.throw(errMsg);
    });
  }

  constructor(private http:Http) { }

}
