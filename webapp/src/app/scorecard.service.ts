import {Injectable} from '@angular/core';
import {Scorecard} from "./scorecard";
import {Observable} from "rxjs";
import {Http, Response, RequestOptions, Headers} from "@angular/http";
import {environment} from "../environments/environment";
import {Game} from "./game";
import {Link} from "./link";

@Injectable()
export class ScorecardService {

  getScorecardsByGame(game:Game):Observable<Scorecard[]>{
    let link:Link = game.links.find(link=>link.rel=="scorecards")
    if(link==null) return Observable.throw("game does not have a link with rel \"scorecards\"");
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
        } else {
          errMsg = error.message ? error.message : error.toString();
        }
        console.error(errMsg);
        return Observable.throw(errMsg);
      });
  }

  constructor(private http: Http) {
  }

}
