import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {Match} from "./match";
import {Link} from "./link";
import {Event} from "./event"
import {Http, RequestOptions, Headers, Response} from "@angular/http";

@Injectable()
export class MatchService {

  getMatches(event: Event): Observable<Match[]> {
    let link: Link = event.links.find(link => link.rel == "matches");
    if (link == null) return Observable.throw("game does not have link with rel \"matches\"");
    return this.http.get(link.href,
                  new RequestOptions({
                    headers: new Headers([
                      {'accept': 'application/vnd.robocubs-v1+json'}
                    ])
                  }))
        .map(res => res.json().data).catch(error => {
      let errMsg: string;
      if (error instanceof Response) {
        const body = error.json() || '';
        const err = body.error || JSON.stringify(body);
        errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
        errMsg += JSON.stringify(error);
      } else {
        errMsg = error.message ? error.message : error.toString();
      }
      console.error("failed to retrieve matches");
      console.error(errMsg);
      return Observable.throw(errMsg);
    });
  }

  createMatch(event:Event,match:Match):Observable<Match>{
    let link: Link = event.links.find(link => link.rel == "matches");
    if (link == null) return Observable.throw("game does not have link with rel \"matches\"");
    return this.http.post(link.href,match,new RequestOptions({
      headers: new Headers([
        {'accept': 'application/vnd.robocubs-v1+json'}
      ])
    })).do(res=>console.log(res.text())).map(res => res.json()).catch(error => {
      let errMsg: string;
      if (error instanceof Response) {
        const body = error.json() || '';
        const err = body.error || JSON.stringify(body);
        errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
        errMsg += JSON.stringify(error);
      } else {
        errMsg = error.message ? error.message : error.toString();
      }
      console.error("failed to create match");
      console.error(errMsg);
      return Observable.throw(errMsg);
    });
  }

  constructor(private http: Http) { }

}
